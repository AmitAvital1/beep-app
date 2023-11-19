package beep.app.util;

public class Constants {
    // Server resources locations
    public final static String BASE_DOMAIN = "10.0.2.2";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/beep";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN = FULL_SERVER_PATH + "/login";
    public final static String CODE = FULL_SERVER_PATH + "/code/";
    public final static String REGISTER = FULL_SERVER_PATH + "/register";
    public final static String IS_USERS = FULL_SERVER_PATH + "/has-user/list";
}
