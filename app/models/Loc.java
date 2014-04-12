package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import java.util.List;

import javax.persistence.Id;

public class Loc extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    public long lat;
    public long lng;

    public String nation;
    public String province;
    public String city;
    public String address;

    public int type;
    // public int checkin_count;
    // public double rate;

    /**
     * MD5 value made from (name + lat + lng + nation + province + city +
     * address), for quick query
     */
    @Constraints.Required
    public int md5;

    public Loc() {
    }

    public Loc(String name, long lat, long lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public Loc(String name, long lat, long lng, String nation, String province,
            String city, String address, int md5) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.nation = nation;
        this.province = province;
        this.city = city;
        this.address = address;
        this.md5 = md5;
    }

    public static Finder<Long, Loc> find = new Finder<Long, Loc>(Long.class,
            Loc.class);

    public static Loc checkExistence(int md5) {
        return find.where().eq("md5", md5).findUnique();
    }

    public static Loc findByMd5(int md5) {
        return checkExistence(md5);
    }

    public static List<Loc> all() {
        return find.all();
    }

    public static void create(Loc loc) {
        loc.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }
}
