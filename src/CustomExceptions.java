class DataLoadingException extends Exception {
    public DataLoadingException(String message) {
        super(message);
    }

    public DataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}

class DataSavingException extends Exception {
    public DataSavingException(String message) {
        super(message);
    }

    public DataSavingException(String message, Throwable cause) {
        super(message, cause);
    }
}

class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}