//@RestController
//@RequestMapping("/api/mail")
//@RequiredArgsConstructor
//public class MailController {
//    private final MailService mailService;
//
//    @PostMapping("/send")
//    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
//        mailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
//        return ResponseEntity.ok("Email sent successfully");
//    }
//
//    @PostMapping("/send-with-attachment")
//    public ResponseEntity<String> sendEmailWithAttachment(@RequestBody EmailAttachmentRequest request) {
//        mailService.sendEmailWithAttachment(
//            request.getTo(),
//            request.getSubject(),
//            request.getBody(),
//            request.getAttachmentPath()
//        );
//        return ResponseEntity.ok("Email with attachment sent successfully");
//    }
//}