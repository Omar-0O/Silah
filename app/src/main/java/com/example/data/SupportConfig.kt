package com.example.data

/**
 * Centralized Developer Support Payment Configuration.
 * 
 * IMPORTANT SECURITY & DESIGN RULES:
 * 1. This contains public payment destination details, NOT secrets.
 * 2. Never expose private API keys, passwords, PINs, or OTPs.
 * 3. Keeps payment information centralized and easy to update without touching UI components.
 * 4. Strictly for voluntary developer support for "Silah".
 */
data class InstapayConfig(
    val enabled: Boolean = true,
    val ipa: String = "omar-0o@instapay",
    val paymentLink: String = "https://ipn.eg/S/omar-0o/instapay/1avNS6"
)

data class WalletMethod(
    val id: String = "vodafone_cash",
    val name: String = "Vodafone Cash",
    val nameAr: String = "فودافون كاش (Vodafone Cash)",
    val enabled: Boolean = true,
    val phoneNumber: String = "01068888907"
)

data class SupportConfig(
    val instapay: InstapayConfig = InstapayConfig(),
    val wallets: List<WalletMethod> = listOf(
        WalletMethod(
            id = "vodafone_cash",
            name = "Vodafone Cash",
            nameAr = "فودافون كاش",
            enabled = true,
            phoneNumber = "01068888907"
        )
    )
) {
    companion object {
        val DEFAULT = SupportConfig()
    }
}
