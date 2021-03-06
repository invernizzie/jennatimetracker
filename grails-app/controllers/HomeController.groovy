class HomeController extends BaseController {

    private static final int MAX_PROJECTS_TO_SHOW = 5

    def companyService

    def beforeInterceptor = [action:this.&auth]

    def auth() {
         try {
          findLoggedUser()
          return true
         } catch (Exception e){
          redirect(controller:'login', action:'auth')
          return false
         }
    }

    def index = {
        if (isAuthenticated()) {
            User user = findLoggedUser()
            Company company = user.company
            Date today = new Date().clearTime()
            def startDate = today - 6
            def endDate = today + 1
            def nextWeekStart = endDate
            def nextWeekEnd = nextWeekStart + 7
            def birthdays = companyService.listBirthdays(company, nextWeekStart, nextWeekEnd)
            def newProjects = companyService.listNewProjects(company, startDate, endDate)
            def timeSpent = companyService.sumTimeSpent(company, startDate, endDate)
            def timeSpentByProject = companyService.sumTimeSpentByProject(company, startDate, endDate)
            def timeSpentTop = timeSpentTop(timeSpent, timeSpentByProject)
            def efforts = Effort.findAll('from Effort as e where e.date >= :fromDate and e.date < :toDate and e.user = :user',
                    [fromDate: startDate, toDate: endDate, user: user]
            )
            def dates = startDate..<endDate
            def projects = efforts.groupBy { it.assignment.project }.keySet().sort { it.name }
            def orderedEfforts = []
            projects.eachWithIndex { project, projectIndex ->
                orderedEfforts << []
                dates.eachWithIndex { date, dateIndex ->
                    orderedEfforts[projectIndex] << findTimeSpentByDateAndProject(efforts, date, project)
                }
            }
            def myProjects = companyService.listProjectsByUser(user, startDate, endDate)
            def myPartners = companyService.listPartners(user, startDate, endDate)
            def knowledge = companyService.listKnowledge(company, startDate, endDate)
            return [birthdays: birthdays, newProjects: newProjects, timeSpentByProject: timeSpentByProject,
                    timeSpentTop: timeSpentTop, orderedEfforts: orderedEfforts, projects: projects, dates: getWeekDayNames(dates),
                    myProjects: myProjects[0..<Math.min(8, myProjects.size())], myPartners: myPartners[0..<Math.min(8, myPartners.size())], startDate: startDate,
                    endDate: endDate, knowledge: knowledge[0..<Math.min(6, knowledge.size())]]
        } else {
            redirect(controller: 'login')
            return
        }
    }

    private timeSpentTop(timeSpent, timeSpentByProject) {
        def orderedProjects = timeSpentByProject.sort { it.timeSpent }.reverse().collect {
            new Expando(projectName: it.project.name, timePct: it.timeSpent * 100 / timeSpent)
        }
        def top = orderedProjects[0..<Math.min(MAX_PROJECTS_TO_SHOW, orderedProjects.size())]
        if (orderedProjects.size() > MAX_PROJECTS_TO_SHOW) {
            def othersTime = 100.0 - top.inject(0.0) { acum, it ->
                acum + it.timePct
            }
            top << new Expando(projectName: g.message(code: 'dashboard.projects.other'), timePct: othersTime)
        }
        return top
    }

    private findTimeSpentByDateAndProject(efforts, date, project) {
        def effort = efforts.findAll {
            it.date.clearTime() == date && it.assignment.project.id == project.id
        }.inject(0.0) { acum, actual ->
            acum + actual.timeSpent
        }
        return effort
    }

    private getWeekDayNames(dates) {
        def cal = Calendar.getInstance()
        dates.collect {
            cal.time = it
            g.message(code: 'dashboard.weekDayName.' + cal.get(Calendar.DAY_OF_WEEK))
        }
    }
}
