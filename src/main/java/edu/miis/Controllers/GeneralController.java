package edu.miis.Controllers;


import javax.servlet.http.HttpSession;

import edu.miis.Entities.UserBean;
import org.springframework.stereotype.Controller;
//import org.springframework.stereotype.Controller;
//import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value="/")
public class GeneralController {

	
	@RequestMapping(value="test",method=RequestMethod.GET)
	public ModelAndView test(ModelAndView model,HttpSession session){
		
//		model.addObject(attributeName, attributeValue)
		model.addObject("info","miao");
		session.setAttribute("msg", "this is session msg");
		model.setViewName("test");
		return model;
		
	}
	
	
	//首页
	@RequestMapping(value="home",method=RequestMethod.GET)
	public ModelAndView Home(ModelAndView model){
		model.setViewName("catopia");
		
		return model;
		
	}
	
	
	@RequestMapping(value="signup",method=RequestMethod.GET)
	public ModelAndView Signup(){
		ModelAndView view = new ModelAndView();
		view.setViewName("signup");
		UserBean user = new UserBean();
		view.addObject("user",user);
		return view;
	}
	
	@RequestMapping(value="login", method=RequestMethod.GET)
	public String Login(Model model){
		UserBean user = new UserBean();
		Model user1 = model.addAttribute("user", user);
		return "login";
		
	}

@RequestMapping(value="exit")
	public String logout(HttpSession session){
		session.invalidate();
		return "redirect:/login";
	}


	
}
