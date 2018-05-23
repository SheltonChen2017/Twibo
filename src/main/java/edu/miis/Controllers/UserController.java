package edu.miis.Controllers;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import edu.miis.DataTransferPojo.ArticleTransferPojo;
import edu.miis.DataTransferPojo.CommentTransferPojo;
import edu.miis.Entities.*;
import edu.miis.Service.IUserServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.Security;
import java.util.*;

@Controller("userController")
@RequestMapping(value="/")
public class UserController {

    @Resource(name = "userServ")
    private IUserServ serv;
    @Autowired
    private Producer captchaProducer;

    @RequestMapping(value = "addUser")
    public String add(ModelAndView model, @Validated @ModelAttribute("user") UserBean user, BindingResult bindingresult, HttpSession session, @RequestParam String Q1, @RequestParam String A1) {
        System.out.println("InControllerAddUser");
        if (bindingresult.hasErrors()) {
            for (FieldError fieldError : bindingresult.getFieldErrors()) {
                System.out.println(fieldError);
            }

            return "signup";
        }
//        System.out.println(user.getUsername());
//    System.out.println(A1);
//        System.out.println(Q1);
        SecurityQuestion sq = new SecurityQuestion();
        sq.setQuestion(Q1);
        sq.setAnswer(A1);
        sq.setOwnerr(user);
        user.addQA(sq);


        boolean register = serv.Register(user);
//        model.setViewName("login");
        session.setAttribute("user", user);
        return "redirect:/mainBlog";
    }

    @RequestMapping(value = "mainBlog")
    public String Mainblog(@SessionAttribute("user") UserBean userbean, Model model,HttpSession session) {
        return "mainBlog";
    }

    @RequestMapping(value = "loginto")
    public String login(@Validated @ModelAttribute("user") UserBean user, BindingResult bindingresult, Model model, HttpSession session) {

        boolean login = serv.Login(user);

        if (login == false) {
            model.addAttribute("msg", "wrong username password combination");

            return "login";

        } else {

            model.addAttribute("msg", "login is successful");
            UserBean user2 = serv.selectByUsername(user.getUsername());
//            int countPage=0;
//            session.setAttribute("countPage",countPage);
            session.setAttribute("user", user2);
        }


        return "redirect:/mainBlog";
    }

    @RequestMapping(value = "verify", method = RequestMethod.GET)
    public void verifyUsername(@RequestParam String username, HttpServletResponse response) throws IOException {
        boolean b = serv.verifyUsername(username);

        if (b == true) {
            response.getWriter().write("valid username");
        } else {
            response.getWriter().write("not valid username");
        }

    }

    @RequestMapping(value = "Retrieve", method = RequestMethod.GET)
    public String retrievePassword() {

        return "passwordRetrieval";
    }

    @RequestMapping(value = "veriCode", method = RequestMethod.GET)
    public ModelAndView generationVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();


        response.setDateHeader("Expires", 0);

        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");

        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");

        // return a jpeg
        response.setContentType("image/jpeg");

        // create the text for the image
        String capText = captchaProducer.createText();

        // store the text in the session
        session.setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);

//    String code = (String)session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
//    System.out.println("******************验证码是: " + code + "******************");
        // create the image with the text
        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();

        // write the data out
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
        return null;

    }

    @RequestMapping(value = "veriCheck", method = RequestMethod.GET)
    public ModelAndView checkVerify(@RequestParam String num, HttpSession session, HttpServletResponse response) throws IOException {
        String code = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (num.equals(code)) {
            response.getWriter().write("Good, we know you are not a bot");
        } else {

            response.getWriter().write("Stupid bots. Your poor attempt to sabotage our net security has failed, utterly");

        }

        return null;
    }

    @RequestMapping(value = "retrieveCheckName", method = RequestMethod.GET)
    public ModelAndView retriveCheckName(@RequestParam String username, HttpServletResponse response) throws IOException {
        boolean b = serv.verifyUsername(username);

        if (b == false) {
//        response.getWriter().write("no");
            //找出所有问题
            String state = serv.loadUsername(username);
            response.getWriter().write(state);

        } else {
            response.getWriter().write("no");
        }
        return null;
    }

    @RequestMapping(value = "verifyQA", method = RequestMethod.GET)
    public String verifyQA(@RequestParam String username, @RequestParam String question, @RequestParam String answer, HttpSession session, HttpServletRequest request) {

        boolean flag = serv.verifyQA(username, question, answer);
//        System.out.println("look at here!!!!!!!!!!!!!!!!!!!!!!!!" + flag);
        if (flag == true) {
            UserBean user = serv.selectByUsername(username);
            session.setAttribute("user", user);

            return "newpassword";

        } else {
            request.setAttribute("msg", "wrong input");

        }
        return "redirect:/passwordRetrieval";
    }

    @RequestMapping(value = "changePassword")
    public String changePassword(@SessionAttribute UserBean user, @RequestParam String password, HttpSession session) {
        System.out.println(password);

        user.setPassword(password);

        serv.updateUser(user);

        session.setAttribute("user", user);

        return "mainBlog";
    }



    @RequestMapping(value = "testLink")
    public String testlink() {
        return "testPathVariable";
    }

    @RequestMapping(value = "publish", method = RequestMethod.POST)
    public String Publish(@RequestParam String content, @SessionAttribute UserBean user,HttpSession session, Model model) {

        UserBean ub = serv.selectByUsername(user.getUsername());

        Article article = new Article();
        article.setContent(content);
//        article.setTitle(title);
        article.setAuthor(ub);
        ub.getArticles().add(article);

//        System.out.println("____________________________");
        serv.updateUser(ub);

        session.setAttribute("user", ub);
        return "redirect:mainBlog";
    }

    @RequestMapping(value = "user/visituserID={userID}", method = RequestMethod.GET)
    public String visit(HttpSession session, @PathVariable Long userID, Model model) {

        UserBean ub = serv.loadUser(userID);

        model.addAttribute("individual", ub);

        return "user/individualpage";
    }
    @RequestMapping(value="Posts/articleId={articleId}",method=RequestMethod.GET)
    public String toViewPost(HttpSession session, @PathVariable Long articleId, Model model){
        System.out.println(articleId);

        Article article = serv.loadOneArticle(articleId);
model.addAttribute("temporaryArticle",article);
//       ("temporaryArticleId",articleId);
model.addAttribute("temporaryArticleId",articleId);
//System.out.println(ar)
        return "/Posts/IndividualPost.html";
    }



    @RequestMapping(value = "checkIfFollowed")
    public ModelAndView checkFollow(@RequestParam Long visitID, @SessionAttribute UserBean user, HttpServletResponse response, HttpSession session) throws IOException {
        System.out.println("in checkFollow: userID acquired in: " + visitID);
        UserBean account = serv.loadUser(user.getId());
        Set<Relationship> rls = account.getRelationships();
        Relationship rlt = new Relationship();
        rlt.setUser(account);
        rlt.setFid(visitID);

//        System.out.println(rls.contains(rlt));

        if(rls.contains(rlt)){

            response.getWriter().write("exists already. deleted");
            rls.remove(rlt);

        }else{
            response.getWriter().write("not exists. Added");
            rls.add(rlt);
        }

        serv.updateUser(account);

        return null;
    }

    @RequestMapping(value="comment")
    public String sendComment(@RequestParam Long articleId, @RequestParam String content,@SessionAttribute UserBean user){

        UserBean ub = serv.selectByUsername(user.getUsername());

        serv.saveComent(articleId,content,ub);

        return "redirect:Posts/articleId="+articleId;
    }

    @RequestMapping(value="loadComments",method=RequestMethod.GET)
    public String queryCommentsByTime(@RequestParam String temporaryArticleId,@SessionAttribute UserBean user,HttpServletResponse response,HttpSession session) throws IOException {
// 不同之处在于 加载评论时，是按id加载，而非时间。

        System.out.println("________________"+temporaryArticleId);
//        session.setAttribute("");
        Long l = Long.valueOf(temporaryArticleId);
        session.setAttribute("temporaryArticleId",l);
        List<Comment> comments = serv.queryCommentByTime(l, 0L);


        ArrayList<CommentTransferPojo> cTransfers = new ArrayList<>();
        //顺序：
//        private String content;
//        private Long articleId;
//        private Long commentId;
//        private Long authorId;
//       private Long authorName;

        for(Comment c:comments){
            CommentTransferPojo ctp = new CommentTransferPojo(c.getContent(), c.getArticle().getId(), c.getId(), c.getAuthor().getId(),c.getAuthor().getUsername());

            cTransfers.add(ctp);

        }

        Collections.sort(cTransfers, new Comparator<CommentTransferPojo>() {
            @Override
            public int compare(CommentTransferPojo a1, CommentTransferPojo a2) {
                if(a1.getCommentId()>a2.getCommentId()){
                    return 1;
                } else if(a1.getCommentId()==a2.getCommentId()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        if(cTransfers.size()-1>0){

            session.setAttribute("entryIndexComment",cTransfers.get(cTransfers.size()-1).getCommentId());
//            session.setAttribute("temporaryArticleId")
        }


        Gson gson = new Gson();
        String s = gson.toJson(cTransfers);
        response.getWriter().write(s);

        return null;
    }

    @RequestMapping(value="continueLoadingComments",method=RequestMethod.GET)
    public String loadCommentsContinue(HttpServletResponse response,HttpSession session, @SessionAttribute UserBean user, @SessionAttribute Long entryIndexComment,@RequestParam Long temporaryArticleId) throws IOException {

        List<Comment> comments = serv.queryCommentByTime(temporaryArticleId, 0L);


        ArrayList<CommentTransferPojo> cTransfers = new ArrayList<>();
        for(Comment c:comments){
            CommentTransferPojo ctp = new CommentTransferPojo(c.getContent(), c.getArticle().getId(), c.getId(), c.getAuthor().getId(),c.getAuthor().getUsername());

            cTransfers.add(ctp);

        }

        Collections.sort(cTransfers, new Comparator<CommentTransferPojo>() {
            @Override
            public int compare(CommentTransferPojo a1, CommentTransferPojo a2) {
                if(a1.getCommentId()>a2.getCommentId()){
                    return 1;
                } else if(a1.getCommentId()==a2.getCommentId()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        if(cTransfers.size()-1>0){

            session.setAttribute("entryIndexComment",cTransfers.get(cTransfers.size()-1).getCommentId());
//            session.setAttribute("temporaryArticleId")
        }


        Gson gson = new Gson();
        String s = gson.toJson(cTransfers);
        response.getWriter().write(s);

        return null;
    }


    @RequestMapping(value="loadPage",method=RequestMethod.GET)
    public String queryByTime(HttpSession session,@SessionAttribute UserBean user,HttpServletResponse response) throws IOException {
        UserBean ub = serv.selectByUsername(user.getUsername());
        Date lastLog = ub.getLastLoginTime();
        Set<Relationship> rls = user.getRelationships();
        ArrayList<Article> articles = new ArrayList<>();
        ArrayList<ArticleTransferPojo> transfers = new ArrayList<>();
        for(Relationship rlt:rls){

            Long fid = rlt.getFid();
            System.out.println(fid+"hahahahahahahahahahahaha");
            List<Article> list= serv.queryByTime(fid,lastLog);
//            ArrayList<ArticleTransferPojo> articleTransferrable = new ArrayList<>();
            for(Article a:list){
//                System.out.println(a.getContent());
                articles.add(a);
                ArticleTransferPojo atp = new ArticleTransferPojo(a.getAuthor().getUsername(), a.getAuthor().getId(), a.getContent(), a.getId(),a.getInsertTime());
                transfers.add(atp);

            }
        }

        Set<Article> articlesUser = user.getArticles();

        for(Article a:articlesUser){

            articles.add(a);
            ArticleTransferPojo atp = new ArticleTransferPojo(a.getAuthor().getUsername(), a.getAuthor().getId(), a.getContent(), a.getId(),a.getInsertTime());
            transfers.add(atp);
        }

        Collections.sort(transfers, new Comparator<ArticleTransferPojo>() {
        @Override
        public int compare(ArticleTransferPojo a1, ArticleTransferPojo a2) {
            if(a1.getArticleId()<a2.getArticleId()){
                return 1;
            } else if(a1.getArticleId()==a2.getArticleId()){
return 0;
            }else{
                return -1;
            }
        }
    });

        Collections.sort(articles, new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                if(a1.getId()<a2.getId()){
                    return 1;
                } else if(a1.getId()==a2.getId()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        Gson gson = new Gson();
        String s = gson.toJson(transfers);

        response.getWriter().write(s);
        if(articles.size()-1>0) {
            session.setAttribute("paramTime", articles.get(articles.size() - 1).getInsertTime());
        }

        return null;
    }

//    public


    @RequestMapping(value="loadingContinue",method=RequestMethod.GET)
    public String continuousLoadingPosts(@SessionAttribute Date paramTime,@SessionAttribute UserBean user, HttpSession session,HttpServletResponse response) throws IOException {

        Set<Relationship> rls = user.getRelationships();
        ArrayList<Article> articles = new ArrayList<>();
        ArrayList<ArticleTransferPojo> transfers = new ArrayList<>();

        for(Relationship rlt:rls){

            Long fid = rlt.getFid();
//            System.out.println(fid+"hahahahahahahahahahahaha");
            List<Article> list= serv.queryByTime(fid,paramTime);
//            ArrayList<ArticleTransferPojo> articleTransferrable = new ArrayList<>();
            for(Article a:list){
//                System.out.println(a.getContent());
                articles.add(a);
                ArticleTransferPojo atp = new ArticleTransferPojo(a.getAuthor().getUsername(), a.getAuthor().getId(), a.getContent(), a.getId(),a.getInsertTime());
                transfers.add(atp);

            }
        }

        Collections.sort(articles, new Comparator<Article>() {
            @Override
            public int compare(Article a1, Article a2) {
                if(a1.getId()<a2.getId()){
                    return 1;
                } else if(a1.getId()==a2.getId()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        Collections.sort(transfers, new Comparator<ArticleTransferPojo>() {
            @Override
            public int compare(ArticleTransferPojo a1, ArticleTransferPojo a2) {
                if(a1.getArticleId()<a2.getArticleId()){
                    return 1;
                } else if(a1.getArticleId()==a2.getArticleId()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        Gson gson = new Gson();
        String s = gson.toJson(transfers);

        response.getWriter().write(s);
        if(articles.size()-1>0) {
            session.setAttribute("paramTime", articles.get(articles.size() - 1).getInsertTime());
        }
        session.setAttribute("user",user);
        return null;

    }

@RequestMapping(value="/loadIndividualPage",method=RequestMethod.GET)
    public String loadIndividualPage(@RequestParam String visitID, HttpServletResponse response) throws IOException {

    Long userId = Long.valueOf(visitID);
    Date currentDate = new Date();
   List<ArticleTransferPojo> list= serv.loadIndividualPage(userId,currentDate);
    Gson gson = new Gson();
    String s = gson.toJson(list);
    response.getWriter().write(s);
    return null;
}

    @RequestMapping(value="/search",method=RequestMethod.POST)
    public ModelAndView search(@RequestParam String name, @SessionAttribute UserBean user){
    List<UserBean> userList= serv.searchByName(name);

    ModelAndView view = new ModelAndView();
view.setViewName("searchPage");
view.addObject("users",userList);
       return view;
    }

    //转发功能：
    /*
    * 方式：
    * 1） 将被转发内容的article content, article id, userID, userName 记录。
    * 2） 建立“reference”表
    * 3） 表中含有：forwarder, article,id
    *
    * */
@RequestMapping(value="repost",method=RequestMethod.POST)
    public String repost(@SessionAttribute UserBean user,@RequestParam String comment,@RequestParam Long articleId){
    UserBean forwarder = serv.selectByUsername(user.getUsername());
    Repost repost = new Repost();
    repost.setForwarder(forwarder);
    Article ref = serv.loadArticleById(articleId);
    repost.setRef(ref);
    repost.setComment(comment);
serv.saveRepost(repost);
    return "redirect:/mainBlog";
    }



}