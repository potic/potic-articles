package me.potic.articles.utils

import org.apache.commons.lang3.StringUtils

class Utils {

    static String maskForLog(String secret) {
        if (secret == null || secret.length() == 0) {
            return ''
        }
        if (secret.length() == 1) {
            return '*'
        }
        if (secret.length() <= 3) {
            return "${secret[0]}*"
        }
        return "${secret[0]}${secret[1]}${StringUtils.abbreviate('*' * (secret.length() - 2), 10)}"
    }
}
