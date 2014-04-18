package models;

import com.avaje.ebean.Page;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Checkin extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Column(name = "created")
    public java.util.Date created;

    public String shout;
    public int score;

    public Long user_id;
    public Long loc_id;

    /**
     * Generic query helper for entity Checkin with id Long
     */
    public static Finder<Long, Checkin> find = new Finder<Long, Checkin>(
            Long.class, Checkin.class);

    /**
     * @return All checkins in the table
     */
    public static List<Checkin> all() {
        return find.all();
    }

    public static void create(Checkin checkin) {
        checkin.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    /**
     * Return a page of checkin
     * 
     * @param page
     *            Page to display
     * @param pageSize
     *            Number of checkins per page
     * @param sortBy
     *            Checkin property used for sorting
     * @param order
     *            Sort order (either asc or desc)
     * @param filter
     *            Filter applied on the name column
     */
    public static Page<Checkin> pageByUser(int page, int pageSize,
            String sortBy, String order, String userId) {
        return find.where().eq("user_id", userId).orderBy(sortBy + " " + order)
                .findPagingList(pageSize).setFetchAhead(false).getPage(page);
    }

    public static Page<Checkin> pageByLoc(int page, int pageSize,
            String sortBy, String order, String locId) {
        return find.where().eq("loc_id", locId).orderBy(sortBy + " " + order)
                .findPagingList(pageSize).setFetchAhead(false).getPage(page);
    }
}
