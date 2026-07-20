# Sanwo SDK ProGuard rules

# Keep JavaScript interface bridge
-keepclassmembers class com.sanwohq.android.SanwoCheckoutActivity$SanwoJSBridge {
    public *;
}

# Keep data classes used for serialization
-keep class com.sanwohq.android.CheckoutResult$* { *; }
-keep class com.sanwohq.android.SanwoEvent { *; }
