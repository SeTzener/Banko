package com.banko.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURLErrorFailingURLErrorKey
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKUIDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWindowFeatures
import platform.darwin.NSObject

private const val REDIRECT_PREFIX = "Banko://"

/**
 * Small non-Compose holder so the WebView bookkeeping does not trigger
 * recompositions. [remember] keeps one instance for the lifetime of the view.
 */
private class Ref<T>(var value: T)

/**
 * WKWebView reports a redirect to a scheme it cannot load (our `Banko://`
 * callback) as a failed navigation instead of asking the navigation delegate.
 * The failing URL is still available in the error, so it can be handled here.
 */
@OptIn(ExperimentalForeignApi::class)
private fun NSError.failingUrlString(): String? {
    val info = userInfo
    return when (val failingUrl = info[NSURLErrorFailingURLErrorKey]) {
        is NSURL -> failingUrl.absoluteString
        is String -> failingUrl
        else -> null
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun BankLinkingWebView(
    url: String,
    onRedirectDetected: (url: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val configuration = remember { WKWebViewConfiguration() }
    val currentOnRedirectDetected = rememberUpdatedState(onRedirectDetected)
    val loadedUrl = remember { Ref<String?>(null) }
    val redirectHandled = remember { Ref(false) }

    fun reportRedirect(redirectUrl: String) {
        if (redirectHandled.value) return
        redirectHandled.value = true
        currentOnRedirectDetected.value(redirectUrl)
    }

    fun reportRedirectIfCallback(failingUrl: String?) {
        if (failingUrl != null && failingUrl.startsWith(REDIRECT_PREFIX, ignoreCase = true)) {
            reportRedirect(failingUrl)
        }
    }

    val navigationDelegate = remember {
        object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit
            ) {
                val requestUrl = decidePolicyForNavigationAction.request.URL?.absoluteString.orEmpty()
                if (requestUrl.startsWith(REDIRECT_PREFIX, ignoreCase = true)) {
                    reportRedirect(requestUrl)
                    decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                } else {
                    decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                }
            }

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didFailProvisionalNavigation: WKNavigation?,
                withError: NSError
            ) {
                reportRedirectIfCallback(withError.failingUrlString())
            }

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didFailNavigation: WKNavigation?,
                withError: NSError
            ) {
                reportRedirectIfCallback(withError.failingUrlString())
            }
        }
    }

    // Bank pages occasionally open the return URL in a new window. Loading it
    // in this web view keeps the existing redirect handling in charge instead
    // of silently dropping the navigation.
    val uiDelegate = remember {
        object : NSObject(), WKUIDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                createWebViewWithConfiguration: WKWebViewConfiguration,
                forNavigationAction: WKNavigationAction,
                windowFeatures: WKWindowFeatures
            ): WKWebView? {
                val requestUrl = forNavigationAction.request.URL?.absoluteString.orEmpty()
                if (requestUrl.startsWith(REDIRECT_PREFIX, ignoreCase = true)) {
                    reportRedirect(requestUrl)
                } else {
                    webView.loadRequest(forNavigationAction.request)
                }
                return null
            }
        }
    }

    UIKitView(
        factory = {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = configuration
            ).apply {
                setNavigationDelegate(navigationDelegate)
                setUIDelegate(uiDelegate)
                scrollView.scrollEnabled = true
                scrollView.alwaysBounceVertical = true
                loadedUrl.value = url
                loadRequest(NSURLRequest(uRL = NSURL(string = url)))
            }
        },
        modifier = modifier.fillMaxSize(),
        // Compose's default cooperative gesture handling delays touches and can
        // swallow drags on the WKWebView. Give the web view all gestures so the
        // bank's form can be scrolled to its submit button.
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
        update = { webView ->
            // Only reload when the caller gives us a different authorization
            // URL. Comparing against webView.URL reloaded the bank flow on
            // every recomposition (for example when the keyboard opened).
            if (loadedUrl.value != url) {
                loadedUrl.value = url
                webView.loadRequest(NSURLRequest(uRL = NSURL(string = url)))
            }
        }
    )
}
