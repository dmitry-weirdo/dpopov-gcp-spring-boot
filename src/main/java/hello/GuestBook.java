package hello;

import javax.persistence.*;

@Entity
@Table(name = "guestbook")
public class GuestBook {

    @Id
    @Column(name = "entryID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guestname")
    private String guestName;

    private String content;

    public Long getId() {
        return id;
    }
    public void setId(final Long id) {
        this.id = id;
    }

    public String getGuestName() {
        return guestName;
    }
    public void setGuestName(final String guestName) {
        this.guestName = guestName;
    }

    public String getContent() {
        return content;
    }
    public void setContent(final String content) {
        this.content = content;
    }
}