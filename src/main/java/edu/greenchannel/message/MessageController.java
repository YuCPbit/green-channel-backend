package edu.greenchannel.message;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequirePermission("message:view")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<MessageRecord>> search(
            @RequestParam(required = false) Boolean readStatus,
            @RequestParam(required = false) String messageType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        return ApiResponse.success(service.search(currentUser(request).id(), readStatus, messageType, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCount> unreadCount(HttpServletRequest request) {
        return ApiResponse.success(new UnreadCount(service.unreadCount(currentUser(request).id())));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<MessageRecord> markRead(@PathVariable long id, HttpServletRequest request) {
        return ApiResponse.success(service.markRead(id, currentUser(request).id()));
    }

    @PutMapping("/read-all")
    public ApiResponse<UnreadCount> markAllRead(HttpServletRequest request) {
        int updated = service.markAllRead(currentUser(request).id());
        return ApiResponse.success(new UnreadCount(updated));
    }

    private CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
    }
}
