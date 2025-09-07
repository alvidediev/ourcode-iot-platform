package org.ourcode.rest.imititateWork;


import lombok.RequiredArgsConstructor;
import org.ourcode.service.imitation.ImitationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Имитация работы сервиса. В кафку отправляется 1000 сообщений
 */
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class ImitationWorkRestController {

    private final ImitationService imitationService;

    @PostMapping("/imitate")
    public void startImitateWork() {
        imitationService.startImitation();
    }
}
