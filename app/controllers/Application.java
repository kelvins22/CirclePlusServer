package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Business;
import models.Checkin;
import models.Loc;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MD5Utils;
import utils.TextUtils;
import views.html.index;

import java.util.List;

public class Application extends Controller {

    private static final int OP_OK = 0x0;
    private static final int INPUT_ERROR = 0x01;
    private static final int USER_EXIST = 0x02;

    private static final int STATUS_OK = 0x0;
    private static final int STATUS_INPUT_JSON_ERROR = 0x1;
    private static final int STATUS_EMPTY_ERROR = 0x2;
    private static final int STATUS_USER_EXIST_ERROR = 0x3;
    private static final int STATUS_AUTH_ERROR = 0x4;
    @SuppressWarnings("unused")
    private static final int STATUS_CHECK_IN_ERROR = 0x5;
    private static final int STATUS_TOKEN_ERROR = 0x6;
    private static final int STATUS_NOT_BUSINESS_ERROR = 0x7;

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
        userJson.put("id", auth.id);
        userJson.put("name", auth.name);
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
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_INPUT_JSON_ERROR);
            errorJson.put("message", "Excepting JSON data");
            result.put("error", errorJson);
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
        String locMd5 = newLoc(locName, lat, lng, nation, province, city,
                address, type);

        // about checkin
        long locId = Loc.checkExistence(locMd5).id;
        String checkinName = json.findPath("checkinName").textValue();
        String shout = json.findPath("shout").textValue();
        int score = 5;
        long userId = json.findPath("userId").longValue();
        newCheckin(checkinName, shout, score, userId, locId);

        ObjectNode statusJson = Json.newObject();
        statusJson.put("status", STATUS_OK);
        statusJson.put("message", checkinName + " checkin");
        result.put("ok", statusJson);
        return ok(result);
    }

    public static Result registerBusiness() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        if (json == null) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_INPUT_JSON_ERROR);
            errorJson.put("message", "Excepting JSON data");
            result.put("error", errorJson);
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
        String locMd5 = newLoc(locName, lat, lng, nation, province, city,
                address, type);

        // about business
        long locId = Loc.checkExistence(locMd5).id;
        String businessName = json.findPath("businessName").textValue();
        long userId = json.findPath("userId").longValue();
        newBusiness(businessName, userId, locId);

        // mark this user as business
        User targetUser = User.find.ref(userId);
        targetUser.is_business = true;
        targetUser.update();

        ObjectNode statusJson = Json.newObject();
        statusJson.put("status", STATUS_OK);
        statusJson.put("message", businessName + " register");
        result.put("ok", statusJson);
        return ok(result);
    }

    public static Result listFavorites() {
        ObjectNode result = Json.newObject();

        long id = 0L;
        try {
            id = Long.parseLong(request().getQueryString("id"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (id <= 0L) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_AUTH_ERROR);
            errorJson.put("message", "Id <=0 error, please login again");
            result.put("error", errorJson);
            return badRequest(result);
        }

        String token = request().getQueryString("token");
        User targetUser = User.find.ref(id);
        String username = targetUser.name;
        String password = targetUser.password;
        String targetToken = MD5Utils.hashKey(username + password);
        if (!targetToken.equals(token)) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_TOKEN_ERROR);
            errorJson.put("message", "Token error");
            result.put("error", errorJson);
            return badRequest(result);
        }

        List<Checkin> list = Checkin.pageByUser(0, 20, "created", "DESC",
                String.valueOf(id)).getList();

        ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
        for (Checkin checkin : list) {
            Loc loc = Loc.find.ref(checkin.loc_id);
            ObjectNode locJson = Json.newObject();
            locJson.put("address", loc.address);
            locJson.put("city", loc.city);
            locJson.put("id", loc.id);
            locJson.put("lat", loc.lat);
            locJson.put("lng", loc.lng);
            locJson.put("name", loc.name);
            locJson.put("nation", loc.nation);
            locJson.put("province", loc.province);
            locJson.put("type", loc.type);

            User user = User.find.ref(checkin.user_id);
            ObjectNode userJson = Json.newObject();
            userJson.put("checkinCount", user.checkin_count);
            userJson.put("created", user.created.toString());
            userJson.put("email", user.email);
            userJson.put("followerCount", user.follower_count);
            userJson.put("friendCount", user.friend_count);
            userJson.put("gender", user.gender ? "Male" : "Female");
            userJson.put("id", user.id);
            userJson.put("name", user.name);
            userJson.put("phone", user.phone);
            userJson.put("photo", user.photo);

            ObjectNode checkInJson = Json.newObject();
            checkInJson.put("created", checkin.created.toString());
            checkInJson.put("id", checkin.id);
            checkInJson.put("name", checkin.name);
            checkInJson.put("shout", checkin.shout);
            checkInJson.put("loc", locJson);
            checkInJson.put("user", userJson);

            array.add(checkInJson);
        }
        result.put("ok", array);
        return ok(result);
    }

    public static Result listCheckinHistory() {
        ObjectNode result = Json.newObject();

        long id = 0L;
        try {
            id = Long.parseLong(request().getQueryString("id"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (id <= 0L) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_AUTH_ERROR);
            errorJson.put("message", "Id <=0 error, please login again");
            result.put("error", errorJson);
            return badRequest(result);
        }

        String token = request().getQueryString("token");
        User targetUser = User.find.ref(id);
        String username = targetUser.name;
        String password = targetUser.password;
        String targetToken = MD5Utils.hashKey(username + password);
        if (!targetToken.equals(token)) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_TOKEN_ERROR);
            errorJson.put("message", "Token error");
            result.put("error", errorJson);
            return badRequest(result);
        }

        // check whether this user is business or not
        if (!targetUser.is_business) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_NOT_BUSINESS_ERROR);
            errorJson.put("message", "You are not a business user yet");
            result.put("error", errorJson);
            return badRequest(result);
        }

        long locId = Business.findMyLocId(id);
        List<Checkin> list = Checkin.pageByLoc(0, 20, "created", "DESC",
                String.valueOf(locId)).getList();

        ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
        for (Checkin checkin : list) {
            Loc loc = Loc.find.ref(checkin.loc_id);
            ObjectNode locJson = Json.newObject();
            locJson.put("address", loc.address);
            locJson.put("city", loc.city);
            locJson.put("id", loc.id);
            locJson.put("lat", loc.lat);
            locJson.put("lng", loc.lng);
            locJson.put("name", loc.name);
            locJson.put("nation", loc.nation);
            locJson.put("province", loc.province);
            locJson.put("type", loc.type);

            User user = User.find.ref(checkin.user_id);
            ObjectNode userJson = Json.newObject();
            userJson.put("checkinCount", user.checkin_count);
            userJson.put("created", user.created.toString());
            userJson.put("email", user.email);
            userJson.put("followerCount", user.follower_count);
            userJson.put("friendCount", user.friend_count);
            userJson.put("gender", user.gender ? "Male" : "Female");
            userJson.put("id", user.id);
            userJson.put("name", user.name);
            userJson.put("phone", user.phone);
            userJson.put("photo", user.photo);

            ObjectNode checkInJson = Json.newObject();
            checkInJson.put("created", checkin.created.toString());
            checkInJson.put("id", checkin.id);
            checkInJson.put("name", checkin.name);
            checkInJson.put("shout", checkin.shout);
            checkInJson.put("loc", locJson);
            checkInJson.put("user", userJson);

            array.add(checkInJson);
        }
        result.put("ok", array);
        return ok(result);
    }

    public static Result grantScore() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        if (json == null) {
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status", STATUS_INPUT_JSON_ERROR);
            errorJson.put("message", "Excepting JSON data");
            result.put("error", errorJson);
            return badRequest(result);
        }

        long checkinId = json.findPath("checkinId").longValue();
        int score = json.findPath("score").intValue();
        Checkin checkin = Checkin.find.byId(checkinId);
        checkin.score = score;
        checkin.update();

        ObjectNode statusJson = Json.newObject();
        statusJson.put("status", STATUS_OK);
        statusJson.put("message", "checkin score = " + score);
        result.put("ok", statusJson);
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
        user.is_business = false;
        user.created = new java.util.Date();
        User.create(user);
        return OP_OK;
    }

    private static String newLoc(String name, long lat, long lng,
            String nation, String province, String city, String address,
            int type) {
        String key = MD5Utils.generateLocKey(name, lat, lng, nation, province,
                city, address);
        String md5 = MD5Utils.hashKey(key);
        if (Loc.checkExistence(md5) == null) {
            Loc loc = new Loc(name, lat, lng, nation, province, city, address,
                    type, md5);
            loc.created = new java.util.Date();
            Loc.create(loc);
        }
        return md5;
    }

    private static int newCheckin(String name, String shout, int score,
            long userId, long locId) {
        Checkin checkin = new Checkin();
        checkin.name = name;
        checkin.shout = shout;
        checkin.score = score;
        checkin.created = new java.util.Date();
        checkin.user_id = userId;
        checkin.loc_id = locId;
        Checkin.create(checkin);
        return 0;
    }

    private static int newBusiness(String name, long userId, long locId) {
        Business business = new Business();
        business.name = name;
        business.created = new java.util.Date();
        business.user_id = userId;
        business.loc_id = locId;
        business.checkin_count = 0;
        Business.create(business);
        return 0;
    }
}
