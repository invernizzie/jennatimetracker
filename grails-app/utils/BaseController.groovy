import org.json.simple.JSONObject
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletRequest


/**
 * @author Alejandro Gomez (alejandro.gomez@fdvsolutions.com)
 * Date: Aug 12, 2009
 * Time: 10:40:09 PM
 */
class BaseController {

    MessageSource messageSource

    boolean isAuthenticated() {
        def authPrincipal = SecurityContextHolder?.context?.authentication?.principal
        return authPrincipal != null && authPrincipal != 'anonymousUser'
    }

    String findLoggedUsername() {
        return SecurityContextHolder.context.authentication?.principal?.username
    }

    User findLoggedUser() {
        return User.findByAccount(findLoggedUsername())
    }

    Company findLoggedCompany() {
        return findLoggedUser().company
    }

    def setUpDefaultPagingParams(params, sortProperty = 'name') {
        params.offset = params.offset ? params.offset.toInteger() : 0
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        if (!params.sort) {
            params.sort = sortProperty
        }
        if (!params.order) {
            params.order = 'asc'
        }
    }

    /**
     * Safe way to get a i18n message (doesn't throw exception, but returns a value saying that the translation is missing
     * We should consider take a look at: http://stackoverflow.com/questions/7770616/best-practice-for-keeping-track-of-i18n-labels-that-need-translating-in-grails instead
     */
    @Deprecated
    String getMessage(HttpServletRequest _request, String _msgKey, Object[] _args = null) {
        try {
            return messageSource.getMessage(_msgKey, _args, getLocale(_request))
        } catch (NoSuchMessageException ex) {
            return "Missing message: $_msgKey"
        }
    }

    /**
     * Safe way to get a i18n message (doesn't throw exception, but returns a value saying that the translation is missing
     * We should consider take a look at: http://stackoverflow.com/questions/7770616/best-practice-for-keeping-track-of-i18n-labels-that-need-translating-in-grails instead
     */
    @Deprecated
    String getMessage(HttpServletRequest _request, MessageSourceResolvable _message) {
        try {
            return messageSource.getMessage(_message, getLocale(_request))
        } catch (NoSuchMessageException ex) {
            return "Missing message: $_message"
        }
    }

    def Locale getLocale(HttpServletRequest _request) {
        return RequestContextUtils.getLocale(_request) ?: Locale.default
    }

    JSONObject buildJsonOkResponse(HttpServletRequest _request, MessageSourceResolvable _title, MessageSourceResolvable _message) {
        JSONObject jsonResponse = new JSONObject()
        jsonResponse.put('ok', true)
        jsonResponse.put('title', getMessage(_request, _title))
        jsonResponse.put('message', getMessage(_request, _message))
        return jsonResponse
    }

    JSONObject buildJsonErrorResponse(HttpServletRequest _request, MessageSourceResolvable _message) {
        JSONObject jsonResponse = new JSONObject()
        jsonResponse.put('ok', false)
        jsonResponse.put('message', getMessage(_request, _message))
        return jsonResponse
    }

    JSONObject buildJsonErrorResponse(HttpServletRequest _request, BeanPropertyBindingResult _bindingResult) {
        def errors = [:]
        _bindingResult.allErrors.each { ObjectError error ->
            errors[(error.field)] = getMessage(_request, error)
        }
        JSONObject jsonResponse = new JSONObject()
        jsonResponse.put('ok', false)
        jsonResponse.put('errors', errors)
        return jsonResponse
    }

    MessageSourceResolvable buildMessageSourceResolvable(String _code, Object[] _args = null) {
        String[] codes = new String[1]
        codes[0] = _code
        return new DefaultMessageSourceResolvable(codes, _args as Object[])
    }

}
