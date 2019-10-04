package lol.gilliard.springboot2fa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@Controller
public class WebController {

    private static Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ModelAndView showHomepage(Principal principal){
        if (principal == null){
            return new ModelAndView("home");
        } else {
            return new ModelAndView("home", "user", principal);
        }
    }

    @GetMapping("/user/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "registration";
    }

    @PostMapping("/user/registration")
    public ModelAndView registerNewUser(@ModelAttribute("user") @Valid UserDto userDto, BindingResult result, RedirectAttributes redirect){

        if (result.hasErrors()){
            return new ModelAndView("registration", "user", userDto);
        }

        try {
            userService.createNewUser(userDto);
            redirect.addFlashAttribute("user", userDto);
            return new ModelAndView("redirect:/user/registered");

        } catch (UserService.UserAlreadyExistsException ex){

            userService.loadUserByUsername(userDto.getUsername());

            logger.warn("Attempt to register existing user username: {}", userDto.getUsername());
            result.rejectValue("username", "already.exists");

            return new ModelAndView("registration");
        }
    }

    @GetMapping("/user/registered")
    public String showRegisteredPage(Model model){

        UserDto userDto = (UserDto) (model.asMap().get("user"));

        if (userDto == null){
            // this check prevents people browsing straight to this page
            return "redirect:/user/registration";
        }

        // TODO: (registration) If the userDto has extra things on it that the user will need, we could add them to the model here
        // (see also /src/main/resources/templates/registered.html)

        return "registered";
    }

    @RequestMapping("/login")
    public String showLoginPage(){
        return "login";
    }

    @RequestMapping("/user/home")
    public ModelAndView showUserHome(Principal principal){
        return new ModelAndView("userhome.html", "user", principal);
    }

}