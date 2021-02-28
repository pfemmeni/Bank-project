package se.sensera.banking.exceptions;

public class UserException extends Exception {
    Activity activity;
    UserExceptionType userExceptionType;
    Object[] params;

    public UserException(Activity activity, UserExceptionType userExceptionType, Object[] params) {
        this.activity = activity;
        this.userExceptionType = userExceptionType;
        this.params = params;
    }

    public Activity getActivity() {
        return activity;
    }

    public UserExceptionType getUserExceptionType() {
        return userExceptionType;
    }

    public Object[] getParams() {
        return params;
    }
}
