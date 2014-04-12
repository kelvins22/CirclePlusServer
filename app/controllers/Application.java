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

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result register() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        if (json == null) {
            result.put("error", "Excepting JSON data");
            return badRequest(result);
        }
        String username = json.findPath("username").textValue();
        String password = json.findPath("password").textValue();
        String email = json.findPath("email").textValue();
        boolean gender = json.findPath("gender").booleanValue();
        String phone = json.findPath("phone").textValue();
        // register new user
        int ret = newUser(username, password, email, gender, phone);
        if (ret == INPUT_ERROR) {
            result.put("error", "Username or password is empty");
            return badRequest(result);
        } else if (ret == USER_EXIST) {
            result.put("error",
                    "Username already exist, change another and retry");
            return badRequest(result);
        } else {
            result.put("status", "OK, new user " + username + " is created");
            return ok(result);
        }
    }

    public static Result login() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        if (json == null) {
            result.put("error", "Excepting JSON data");
            return badRequest(result);
        }
        String name = json.findPath("username").textValue();
        String pswd = json.findPath("password").textValue();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pswd)) {
            result.put("error", "Username or password is empty");
            return badRequest(result);
        }
        User auth = User.authenticate(name, pswd);
        if (auth == null) {
            result.put("error", "Username or password incorrect");
            return badRequest(result);
        }
        result.put("username", auth.name);
        result.put("password", auth.password);
        result.put("email", auth.email);
        result.put("phone", auth.phone);
        result.put("photo", auth.photo);
        result.put("gender", auth.gender);
        result.put("created", auth.created.toString());
        result.put("checkinCount", auth.checkin_count);
        result.put("followerCount", auth.follower_count);
        result.put("friendCount", auth.friend_count);
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
