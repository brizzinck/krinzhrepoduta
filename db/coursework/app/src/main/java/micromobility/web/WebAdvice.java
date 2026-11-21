package micromobility.web;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/** Form objects are plain classes with public fields; blank text fields become null. */
@ControllerAdvice
public class WebAdvice {

    /** An unknown id in a GET link: a plain page instead of a raw error. */
    @org.springframework.web.bind.annotation.ExceptionHandler(java.util.NoSuchElementException.class)
    public org.springframework.web.servlet.ModelAndView notFound() {
        org.springframework.web.servlet.ModelAndView mv = new org.springframework.web.servlet.ModelAndView("notfound");
        mv.setStatus(org.springframework.http.HttpStatus.NOT_FOUND);
        return mv;
    }

    @InitBinder
    public void init(WebDataBinder binder) {
        binder.initDirectFieldAccess();
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
