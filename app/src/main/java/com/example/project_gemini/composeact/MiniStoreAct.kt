package com.example.project_gemini.composeact

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.project_gemini.R
import com.example.project_gemini.composeact.ui.theme.Project_GeminiTheme
import com.google.android.gms.ads.*

class MiniStoreAct : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this) { initializationStatus ->
            Log.d("AdMob", "MobileAds initialized: $initializationStatus")
        }

        setContent {
            Project_GeminiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Box container to hold the WebView and TopBar components
                        Box(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                MiniStoreTopBar()
                                WebViewScreen("https://hushh-for-students-store-vone.mini.site")
                            }
                        }
                        // Banner ad positioned at the bottom of the screen
                        BannerAdView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)

                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniStoreTopBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "hushh for students",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "hushh-for-students.mini.store",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = { /* TODO: Handle share action */ }) {
                        Icon(
                            painter = painterResource(R.drawable.share_icon),
                            contentDescription = "Share",
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = true, // Assuming "Online"
                        onCheckedChange = { /* TODO: Handle toggle action */ },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Green,
                            uncheckedThumbColor = Color.Gray
                        )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black
        )
    )
}

@Composable
fun WebViewScreen(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Configure WebView settings
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.allowContentAccess = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.allowFileAccess = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.loadsImagesAutomatically = true

            // Custom WebViewClient to handle URL loading and payment status detection
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    Log.d("WebView", "Navigating to: $url")
                    if (url.contains("payment_success")) {
                        showToast(context, "Payment successful!")
                    } else if (url.contains("payment_failure")) {
                        showToast(context, "Payment failed. Please try again.")
                    }

                    return if (url.startsWith("http") || url.startsWith("https")) {
                        view.loadUrl(url)
                        false
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                        true
                    }
                }
            }


            loadUrl(url)
        }
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                adUnitId = "ca-app-pub-3940256099942544/9214589741"
                // Test Ad Unit ID

                // Set ad size
                setAdSize(AdSize.BANNER)

                // Set AdListener to monitor ad events
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdView", "Ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("AdView", "Ad failed to load: ${adError.message}")
                    }

                    override fun onAdOpened() {
                        Log.d("AdView", "Ad opened")
                    }

                    override fun onAdClicked() {
                        Log.d("AdView", "Ad clicked")
                    }

                    override fun onAdClosed() {
                        Log.d("AdView", "Ad closed")
                    }
                }

                // Load the ad
                val adRequest = AdRequest.Builder().build()
                loadAd(adRequest)
            }
        },
        modifier = modifier
    )
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    Project_GeminiTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    MiniStoreTopBar()
                    WebViewScreen("https://hushh-for-students-store-vone.mini.site")
                }
            }
            // Preview for BannerAdView at the bottom
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}
