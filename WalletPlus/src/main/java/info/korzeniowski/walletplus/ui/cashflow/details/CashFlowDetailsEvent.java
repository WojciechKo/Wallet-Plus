package info.korzeniowski.walletplus.ui.cashflow.details;

public class CashFlowDetailsEvent {
    public static class AmountChanged {

    }

    public static class CommentChanged {
        public String comment;

        public CommentChanged(String comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return comment;
        }
    }

    public static class FromWalletChanged {

    }

    public static class ToWalletChanged {

    }

    public static class CategoryChanged {

    }

    public static class DateChanged {

    }

    public static class TimeChanged {

    }
}
