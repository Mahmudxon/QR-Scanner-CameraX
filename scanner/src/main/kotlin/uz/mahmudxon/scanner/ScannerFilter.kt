package uz.mahmudxon.scanner

interface ScannerFilter {
    fun isAvailable(rawValue: String): Boolean
}