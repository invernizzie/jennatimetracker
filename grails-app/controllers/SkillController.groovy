class SkillController extends BaseController {

    def index = { redirect(action: "list", params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: ["POST","GET"]]

    def beforeInterceptor = [action: this.&auth]

    def auth() {
        try {
            findLoggedUser()
            return true
        } catch (Exception e) {
            redirect(controller: 'login', action: 'auth')
            return false
        }
    }

    def list = {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        [skillInstanceList: Skill.list(params), skillInstanceTotal: Skill.count()]
    }

    def create = {
        def skillInstance = new Skill()
        skillInstance.properties = params
        def levels = ["SR", "SSR", "JR", "Basic"]
        return [skillInstance: skillInstance, levelList: levels]
    }

    def save = {
        def skillInstance = new Skill(params)
        if (!skillInstance.hasErrors() && skillInstance.save()) {
            flash.message = g.message(code: "skill.created", args: [skillInstance])
            redirect(action: "show", id: skillInstance.id)
        }
        else {
            render(view: "create", model: [skillInstance: skillInstance])
        }
    }

    def show = {
        def skillInstance = Skill.get(params.id)
        if (!skillInstance) {
            flash.message = g.message(code: "skill.not.found", args: [params.id])
            redirect(action: "list")
        }
        else {
            return [skillInstance: skillInstance]
        }
    }

    def edit = {
        def skillInstance = Skill.get(params.id)
        if (!skillInstance) {
            flash.message = g.message(code: "skill.not.found", args: [params.id])
            redirect(action: "list")
        }
        else {
            return [skillInstance: skillInstance]
        }
    }

    def update = {
        def skillInstance = Skill.get(params.id)
        if (skillInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (skillInstance.version > version) {
                    skillInstance.errors.rejectValue("version", "skill.optimistic.locking.failure", "Another user has updated this Skill while you were editing")
                    render(view: "edit", model: [skillInstance: skillInstance])
                    return
                }
            }
            skillInstance.properties = params
            if (!skillInstance.hasErrors() && skillInstance.save()) {
                flash.message = g.message(code: "skill.updated", args: [params.id])
                redirect(action: "show", id: skillInstance.id)
            }
            else {
                render(view: "edit", model: [skillInstance: skillInstance])
            }
        }
        else {
            flash.message = g.message(code: "skill.not.found", args: [params.id])
            redirect(action: "edit", id: params.id)
        }
    }

    def delete = {
        def skillInstance = Skill.get(params.id)
        if (skillInstance) {
            try {
                skillInstance.delete()
                flash.message = g.message(code: "skill.deleted", args: [skillInstance])
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = g.message(code: "skill.not.deleted", args: [skillInstance])
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = g.message(code: "skill.not.found", args: [params.id])
            redirect(action: "list")
        }
    }

    def addToMe = {
        def theSkill = Skill.findById(params.id)

        if (!theSkill){
            flash.message = g.message(code: "skill.not.found", args: [params.id])
            render view: 'list', model: [skillInstanceList: Skill.list(params), skillInstanceTotal: Skill.count()]
            return
        }
        //FIXME: This should not be needed, as an Interceptor checks for logged user
        def user = findLoggedUser()
        if (!user) {
            flash.message = "Not user logged"
            render view: 'list', model: [skillInstanceList: Skill.list(params), skillInstanceTotal: Skill.count()]
            return
        }
        // EOFIXME
        user.addToSkills(theSkill)
        flash.message = g.message(code: "skill.added.to.you", args: [theSkill]) 
		redirect(action: "list", params:[max: Math.min(params.max ? params.max.toInteger() : 10, 100), offset: params.offset])
    }
}