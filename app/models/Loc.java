package models;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Loc extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    public String nation;
    public String province;
    public String city;
    public String address;
    /**
     * MD5 value made from (name + lat + lng + nation + province + city +
     * address), for quick query
     */
    public String md5;

    public long lat;
    public long lng;

    public int type;

    @Column(name = "created")
    public java.util.Date created;

    public Loc() {
    }

    public Loc(String name, long lat, long lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public Loc(String name, long lat, long lng, String nation, String province,
            String city, String address, int type, String md5) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.nation = nation;
        this.province = province;
        this.city = city;
        this.address = address;
        this.type = type;
        this.md5 = md5;
    }

    public static Finder<Long, Loc> find = new Finder<Long, Loc>(Long.class,
            Loc.class);

    public static Loc checkExistence(String md5) {
        return find.where().eq("md5", md5).findUnique();
    }

    public static List<Loc> all() {
        return find.all();
    }

    public static List<Loc> getBusinessLoc() {
        String sql = "SELECT loc.id, loc.name, loc.nation, loc.province, "
                + "loc.city, loc.address, loc.lat, loc.lng, loc.type "
                + "FROM loc INNER JOIN business ON loc.id = business.loc_id;";
        RawSql rawSql = RawSqlBuilder.parse(sql).create();
        return find.setRawSql(rawSql).findList();
    }

    public static void create(Loc loc) {
        loc.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }
}
