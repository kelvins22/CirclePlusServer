package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Checkin;
import models.Loc;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MD5Utils;
import utils.TextUtils;
import views.html.index;

public class Application extends Controller {

    private static final int OP_OK = 0x0;
    private static final int INPUT_ERROR = 0x01;
    private static final int USER_EXIST = 0x02;

    private static final int STATUS_OK = 0x0;
    private static final int STATUS_INPUT_JSON_ERROR = 0x1;
    private static final int STATUS_EMPTY_ERROR = 0x2;
    private static final int STATUS_USER_EXIST_ERROR = 0x3;
    private static final int STATUS_AUTH_ERROR = 0x4;

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result register() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        if (json == null) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_INPUT_JSON_ERROR);
            errorJson.put("message", "Excepting JSON data");
            result.put("error", errorJson);
            return badRequest(result);
        }
        String username = json.findPath("name").textValue();
        String password = json.findPath("password").textValue();
        String email = json.findPath("email").textValue();
        String genderStr = json.findPath("gender").textValue();
        boolean gender = "Male".equals(genderStr);
        String phone = json.findPath("phone").textValue();
        // register new user
        int ret = newUser(username, password, email, gender, phone);
        if (ret == INPUT_ERROR) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_EMPTY_ERROR);
            errorJson.put("message", "Username or password is empty");
            result.put("error", errorJson);
            return badRequest(result);
        } else if (ret == USER_EXIST) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_USER_EXIST_ERROR);
            errorJson.put("message",
                    "Username already exist, change another and retry");
            result.put("error", errorJson);
            return badRequest(result);
        } else {
            ObjectNode statusJson = Json.newObject();
            statusJson.put("status", STATUS_OK);
            statusJson.put("message", "New user " + username + " is created.");
            result.put("ok", statusJson);
            return ok(result);
        }
    }

    public static Result login() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        if (json == null) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_INPUT_JSON_ERROR);
            errorJson.put("message", "Excepting JSON data");
            result.put("error", errorJson);
            return badRequest(result);
        }
        String name = json.findPath("name").textValue();
        String pswd = json.findPath("password").textValue();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pswd)) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_EMPTY_ERROR);
            errorJson.put("message", "Username or password is empty");
            result.put("error", errorJson);
            return badRequest(result);
        }
        User auth = User.authenticate(name, pswd);
        if (auth == null) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_AUTH_ERROR);
            errorJson.put("message", "Username or password incorrect");
            result.put("error", errorJson);
            return badRequest(result);
        }
        ObjectNode userJson = Json.newObject();
        userJson.put("name", auth.name);
        userJson.put("password", auth.password);
        userJson.put("email", auth.email);
        userJson.put("phone", auth.phone);
        userJson.put("photo", auth.photo);
        userJson.put("gender", auth.gender ? "Male" : "Female");
        userJson.put("created", auth.created.toString());
        userJson.put("checkinCount", auth.checkin_count);
        userJson.put("followerCount", auth.follower_count);
        userJson.put("friendCount", auth.friend_count);
        // set user JSON as result content
        result.put("ok", userJson);
        return ok(result);
    }

    public static Result checkin() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        if (json == null) {
            result.put("error", "Excepting JSON data");
            return badRequest(result);
        }

        // make it a known location about location
        String locName = json.findPath("locName").textValue();
        long lat = json.findPath("lat").longValue();
        long lng = json.findPath("lng").longValue();
        String nation = json.findPath("nation").textValue();
        String province = json.findPath("province").textValue();
        String city = json.findPath("city").textValue();
        String address = json.findPath("address").textValue();
        int type = json.findPath("type").intValue();
        int locMd5 = newLoc(locName, lat, lng, nation, province, city, address,
                type);

        // about checkin
        long locId = Loc.findByMd5(locMd5).id;
        String checkinName = json.findPath("checkinName").textValue();
        String shout = json.findPath("shout").textValue();
        long userId = json.findPath("userId").longValue();

        // make a checkin object
        Checkin checkin = new Checkin();
        checkin.name = checkinName;
        checkin.shout = shout;
        checkin.user_id = userId;
        checkin.loc_id = locId;
        Checkin.create(checkin);

        result.put("status", checkinName + " checkin");
        return ok(result);
    }

    public static Result listFavorites(long id) {
        ObjectNode result = Json.newObject();
        return ok(result);
    }

    private static int newUser(String username, String password, String email,
            boolean gender, String phone) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return INPUT_ERROR;
        }
        // check existence
        User existUser = User.checkExistence(username);
        if (existUser != null) {
            return USER_EXIST;
        }
        // insert new user into database
        User user = new User(username, password, email, phone, "", gender, 0,
                0, 0);
        user.created = new java.util.Date();
        User.create(user);
        return OP_OK;
    }

    private static int newLoc(String name, long lat, long lng, String nation,
            String province, String city, String address, int type) {
        String key = MD5Utils.generateLocKey(name, lat, lng, nation, province,
                city, address);
        int md5 = MD5Utils.hashKey(key);
        if (Loc.checkExistence(md5) == null) {
            Loc loc = new Loc(name, lat, lng, nation, province, city, address,
                    md5);
            loc.type = type;
            Loc.create(loc);
        }
        return md5;
    }
}
