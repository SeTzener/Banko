package com.banko.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

private const val REDIRECT_PREFIX = "Banko://"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BankLinkingWebView(
    url: String,
    onRedirectDetected: (url: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val configuration = remember { WKWebViewConfiguration() }
    val navigationDelegate = remember {
        object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit
            ) {
                val requestUrl = decidePolicyForNavigationAction.request.URL?.absoluteString ?: ""
                if (requestUrl.startsWith(REDIRECT_PREFIX, ignoreCase = true)) {
                    onRedirectDetected(requestUrl)
                    decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                } else {
                    decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                }
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
                loadRequest(NSURLRequest(uRL = NSURL(string = url)))
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { webView ->
            val currentUrl = webView.URL?.absoluteString
            if (currentUrl != url) {
                webView.loadRequest(NSURLRequest(uRL = NSURL(string = url)))
            }
        }
    )
}
