import SwiftUI
import FirebaseCore
import FirebaseMessaging
import UserNotifications
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

/// Firebase Cloud Messaging setup for iOS.
///
/// Until the app ships with a paid Apple Developer Program membership, EUA
/// expiry messages are delivered as local reminders (see
/// [IosBankReminderScheduler]). Remote FCM delivery is wired up but needs:
///   1. Enrol in the Apple Developer Program (paid) and add the Push
///      Notifications capability to the iosApp target in Xcode.
///   2. Create an APNs authentication key (.p8) in the Apple Developer portal.
///   3. Upload that key in Firebase Console -> Project settings -> Cloud
///      Messaging -> Apple app configuration, together with the Key ID and
///      your Team ID.
///   4. Test on a physical device. The iOS simulator does not receive remote
///      push notifications.
/// Once the entitlement and APNs key are in place, remote delivery works
/// without further code changes; local reminders can then be removed.
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    /// Kept on the delegate so the Kotlin bridge always has a live scheduler.
    private let reminderScheduler = IosBankReminderScheduler()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()

        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self

        // Local reminders are the iOS fallback until a paid Apple Developer
        // Program membership enables remote APNs push.
        BankReminderBridge.shared.delegate = reminderScheduler

        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { granted, error in
            if let error = error {
                print("Banko push: notification permission error: \(error)")
            }
            guard granted else { return }
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }

        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        // Expected until the Push Notifications capability is added with a paid
        // Apple Developer Program membership. See the note above.
        print("Banko push: failed to register for remote notifications: \(error)")
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let fcmToken else { return }
        PushTokenReceiver.shared.handle(token: fcmToken)
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // While the app is visible, explain the push inside the app instead of
        // also showing a banner the user would have to tap.
        let type = notification.request.content.userInfo["type"] as? String
        if PushMessageReceiver.shared.handle(type: type) {
            completionHandler([])
            return
        }

        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let type = response.notification.request.content.userInfo["type"] as? String
        _ = PushMessageReceiver.shared.handle(type: type)
        completionHandler()
    }
}

/// iOS replacement for remote APNs notifications while the app is distributed
/// with a free Apple ID. The shared layer turns the backend's `expiresAt` into
/// warning/expiry reminders; this class persists them with
/// `UNUserNotificationCenter`, so they still fire when the app is closed.
///
/// The `type` userInfo entry matches the FCM payload, which means tapping a
/// reminder reuses the existing in-app explanation dialog.
///
/// Limitation: reminders are only refreshed when the app successfully fetches
/// bank authorizations. If the app never runs before a connection expires, no
/// local notification can be scheduled; the persistent Home banner covers the
/// next time the app opens.
final class IosBankReminderScheduler: NSObject, BankReminderScheduler {
    private let center = UNUserNotificationCenter.current()
    private let identifierPrefix = "banko-eua-"

    func replaceAll(reminders: [BankReminder]) {
        center.getPendingNotificationRequests { [weak self] pending in
            guard let self else { return }

            // Cancel reminders for banks that were re-authorized or removed
            // before adding the fresh set.
            let staleIdentifiers = pending
                .map { $0.identifier }
                .filter { $0.hasPrefix(self.identifierPrefix) }
            self.center.removePendingNotificationRequests(withIdentifiers: staleIdentifiers)
            self.center.removeDeliveredNotifications(withIdentifiers: staleIdentifiers)

            reminders.forEach { self.schedule($0) }
        }
    }

    private func schedule(_ reminder: BankReminder) {
        let fireDate = Date(
            timeIntervalSince1970: TimeInterval(reminder.fireAtEpochMillis) / 1000.0
        )
        // Never fire an immediate notification for an already-past warning.
        guard fireDate > Date() else { return }

        let content = UNMutableNotificationContent()
        if reminder.wireType == "eua_expiry_warning" {
            content.title = "Bank connection expiring soon"
            content.body = "Re-authorize \(reminder.bankName) to keep syncing transactions."
        } else {
            content.title = "Bank connection expired"
            content.body = "Re-authorize \(reminder.bankName) to resume syncing transactions."
        }
        content.sound = .default
        content.userInfo = ["type": reminder.wireType]

        let components = Calendar.current.dateComponents(
            [.year, .month, .day, .hour, .minute, .second],
            from: fireDate
        )
        let trigger = UNCalendarNotificationTrigger(
            dateMatching: components,
            repeats: false
        )
        let request = UNNotificationRequest(
            identifier: reminder.id,
            content: content,
            trigger: trigger
        )
        center.add(request) { error in
            if let error = error {
                print("Banko reminders: failed to schedule \(reminder.id): \(error)")
            }
        }
    }
}
