//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class MailService {
//    private final JavaMailSender mailSender;
//
//    public void sendEmail(String to, String subject, String body) {
//        try {
//            log.info("Sending email to: {}", to);
//
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(body);
//
//            mailSender.send(message);
//
//            log.info("Email sent successfully to: {}", to);
//        } catch (Exception e) {
//            log.error("Failed to send email to: {}", to, e);
//            throw new CustomException(ErrorCode.EMAIL_SENDING_FAILED);
//        }
//    }
//
//    // Method for sending emails with attachments
//    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(body);
//
//            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
//            helper.addAttachment(file.getFilename(), file);
//
//            mailSender.send(message);
//
//            log.info("Email with attachment sent successfully to: {}", to);
//        } catch (Exception e) {
//            log.error("Failed to send email with attachment to: {}", to, e);
//            throw new CustomException(ErrorCode.EMAIL_SENDING_FAILED);
//        }
//    }
//}