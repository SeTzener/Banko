import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @Environment(\.colorScheme) var colorScheme

    private var surfaceColor: Color {
        colorScheme == .dark
            ? Color(red: 0x21/255, green: 0x21/255, blue: 0x21/255)
            : Color(red: 0xFF/255, green: 0xFF/255, blue: 0xF0/255)
    }

    var body: some View {
        ZStack {
            surfaceColor
                .ignoresSafeArea()
            ComposeView()
                .ignoresSafeArea(.keyboard)
        }
    }
}



