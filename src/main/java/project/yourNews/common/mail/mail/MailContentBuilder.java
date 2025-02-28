package project.yourNews.common.mail.mail;

import project.yourNews.domains.notification.dto.NewsListDto;

import java.util.List;

public class MailContentBuilder {

    public static String buildNewsMailContent(String newsName, String postTitle, String postURL) {
        return "<div style=\"font-family: 'Arial', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);\">" +
                "<strong style=\"color: #333; font-size: 20px; display: block; margin-bottom: 15px; text-align: center;\">[" + newsName + "]</strong>" +
                "<span style=\"font-size: 16px; color: #555; line-height: 1.8; display: block; margin-bottom: 20px; text-align: center;\">" + postTitle + "</span>" +
                "<div style=\"text-align: center;\">" +
                "<a href=\"" + postURL + "\" style=\"color: #ffffff; font-weight: bold; text-decoration: none; padding: 12px 24px; background-color: #1a73e8; border-radius: 50px; display: inline-block;\">게시글 링크</a>" +
                "</div>" +
                "</div>";
    }

    public static String buildNewsMailContent(String newsName, List<NewsListDto> newsListDtos) {
        StringBuilder content = new StringBuilder();

        content.append("<div style=\"font-family: 'Arial', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);\">")
                .append("<strong style=\"color: #333; font-size: 20px; display: block; margin-bottom: 15px; text-align: center;\">[Daily - ").append(newsName).append("]</strong>");

        newsListDtos.forEach(news -> {
            content.append("<div style=\"margin-bottom: 15px;\">")
                    .append("<span style=\"font-size: 16px; color: #555; line-height: 1.8; display: block; margin-bottom: 5px; text-align: center;\">")
                    .append(news.getPostTitle()).append("</span>")
                    .append("<div style=\"text-align: center;\">")
                    .append("<a href=\"").append(news.getPostURL()).append("\" style=\"color: #ffffff; font-weight: bold; text-decoration: none; padding: 12px 24px; background-color: #1a73e8; border-radius: 50px; display: inline-block;\">게시글 링크</a>")
                    .append("</div>")
                    .append("</div>");
        });

        content.append("</div>");
        return content.toString();
    }

    public static String buildJobNewsMailContent(String newsName, List<NewsListDto> newsListDtos) {
        StringBuilder content = new StringBuilder();

        content.append("<div style=\"font-family: 'Arial', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);\">")
                .append("<strong style=\"color: #333; font-size: 20px; display: block; margin-bottom: 15px; text-align: center;\">[").append(newsName).append("]</strong>");

        newsListDtos.forEach(news -> {
            content.append("<div style=\"margin-bottom: 15px;\">")
                    .append("<span style=\"font-size: 16px; color: #555; line-height: 1.8; display: block; margin-bottom: 5px; text-align: center;\">")
                    .append(news.getPostTitle()).append("</span>")
                    .append("<div style=\"text-align: center;\">")
                    .append("<a href=\"").append(news.getPostURL()).append("\" style=\"color: #ffffff; font-weight: bold; text-decoration: none; padding: 12px 24px; background-color: #1a73e8; border-radius: 50px; display: inline-block;\">게시글 링크</a>")
                    .append("</div>")
                    .append("</div>");
        });

        content.append("</div>");
        return content.toString();
    }

    public static String buildRegularMailContent(String originalContent, String unsubscribeLink, String emailUsername) {
        return originalContent +
                "<br><p style=\"font-family: 'Arial', sans-serif; font-size: 12px; color: #777; text-align: center; margin-top: 20px;\"><small>소식을 그만 받고 싶으신가요? " +
                "<a href=\"" + unsubscribeLink + "\" style=\"color: #d9534f; text-decoration: underline;\">구독 취소</a></small></p>" +
                "<br>" +
                "<small style=\"font-family: 'Arial', sans-serif; font-size: 12px; color: #999; text-align: center; display: block; margin-top: 10px;\">사용자 : " + emailUsername + "</small>";
    }

    public static String buildUserEmailContent(String emailUsername) {
        return "<small style=\"font-family: 'Arial', sans-serif; font-size: 12px; color: #999; display: block; margin-top: 10px;\">사용자 : " + emailUsername + "</small>";
    }
}
