package com.blink.admin.user.subhandler;

import com.blink.admin.user.SubHandler;
import com.blink.admin.user.UserHandler;
import com.blink.common.ArticleHelper;
import com.blink.shared.admin.ActionResponseMessage;
import com.blink.shared.admin.FileUploadResponseMessage;
import com.blink.shared.admin.article.*;
import com.blink.shared.common.Article;
import com.blink.shared.common.File;
import com.blink.shared.system.InvalidRequest;
import com.blink.utilities.BlinkTime;
import com.google.common.eventbus.Subscribe;

public class ArticleHandler extends SubHandler {
    private ArticleHelper articleHelper;

    public ArticleHandler(UserHandler userHandler) {
        super(userHandler);
        this.articleHelper = new ArticleHelper(adminService.getContext());
    }

    @Subscribe
    public void handleArticlesReq(ArticlesRequestMessage message) throws Exception {
        adminService.sendReply(new ArticlesResponseMessage(articleHelper.getEntities(message.getTimestamp(),
                message.isLess(), message.getLimit()), articleHelper.getEntityCount()));
    }

    @Subscribe
    public void handleArticleKeyCheck(ArticleKeyCheckRequestMessage message) throws Exception {
        logger.info("Article key check: {}", message);
        adminService.sendReply(new ArticleKeyCheckResponseMessage(articleHelper.isKeyAvailable(message.getKey())));
    }

    @Subscribe
    public void handleArticleCreate(CreateArticleRequestMessage message) throws Exception {
        logger.info("Article create request received: {}", message);
        Article article = new Article();
        article.setKey(message.getKey())
                .setTitle(message.getTitle())
                .setDescription(message.getDescription())
                .setAuthor(getUser().getUsername())
                .setActive(false)
                .setTimestamp(BlinkTime.getCurrentTimeMillis());
        articleHelper.saveEntity(article);
        adminService.sendReply(new CreateArticleResponseMessage(message.getKey(), true, "Success"));
    }

    @Subscribe
    public void handleArticleDelete(ArticleDeleteMessage message) throws Exception {
        boolean success = articleHelper.deleteEntity(message.getKey());
        adminService.sendReply(new ArticleDeleteResponeMessage(message.getKey(), success));
        logger.info("Article delete status [sucess={} key={}]", success, message.getKey());
    }

    @Subscribe
    public void handleRawArticleReq(RawArticleRequestMessage message) throws Exception {
        RawArticle rawArticle = articleHelper.getRawArticle(message.getKey());
        Object reply;
        if (rawArticle == null)
            reply = new InvalidRequest("Invalid key");
        else
            reply = new RawArticleResponseMessage(rawArticle.getKey(), rawArticle.getTitle(),
                    rawArticle.getContent());
        adminService.sendReply(reply);
    }

    @Subscribe
    public void handleUpdateArticleReq(UpdateArticleRequestMessage message) throws Exception {
        String desc = articleHelper.updateArticle(message.getKey(), message.getContent());
        adminService.sendReply(new UpdateArticleResponseMessage(desc == null, desc));
    }

    @Subscribe
    public void handleArticleImage(ArticleImageUploadMessage message) throws Exception {
        File file = articleHelper.saveArticleImage(message.getKey(), message.getContent(), message.getFileName());
        adminService.sendReply(new FileUploadResponseMessage(file != null, file));
    }

    @Subscribe
    public void handleArticleCoverUpload(ArticleCoverUploadMessage message) throws Exception {
        File file = articleHelper.saveArticleCover(message.getKey(), message.getContent(), message.getFileName());
        adminService.sendReply(new FileUploadResponseMessage(file != null, file));
    }

    @Subscribe
    public void handleArticleActivate(ArticleActivateMessage message) throws Exception {
        adminService.sendReply(new ActionResponseMessage(articleHelper.toggleArticleState(message.getKey(), true),
                ""));
        logger.info("Article activated for key: {}", message.getKey());
    }

    @Subscribe
    public void handleArticleDeactivate(ArticleDeactivateMessage message) throws Exception {
        adminService.sendReply(new ActionResponseMessage(articleHelper.toggleArticleState(message.getKey(), false),
                ""));
        logger.info("Article deactivated for key: {}", message.getKey());
    }
}
