package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GuestBookController {

    @Autowired
    private GuestBookRepository guestbookRepository;

    @GetMapping("/guestbooks")
    public Page<GuestBook> getGuestBooks(final Pageable pageable) {
        return guestbookRepository.findAll(pageable);
    }
}