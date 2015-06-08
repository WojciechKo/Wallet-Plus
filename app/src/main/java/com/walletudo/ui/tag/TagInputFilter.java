package com.walletudo.ui.tag;

import android.text.InputFilter;
import android.text.Spanned;

public class TagInputFilter {
    public static class SingleTag implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (isAllowed(c)) {
                    builder.append(c);
                }
            }
            return builder.toString();
        }

        protected boolean isAllowed(char c) {
            return Character.isLetterOrDigit(c) || c == '-';
        }
    }

    public static class MultipleTags extends SingleTag {
        @Override
        protected boolean isAllowed(char c) {
            return super.isAllowed(c) || c == ' ';
        }
    }
}
