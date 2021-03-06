<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.menu.administration.reports.users"/></title>
</head>

<body>
<div class="body">
    <h1><span class="style7"><g:message code="user.show" default="User List"/></span></h1>

    <p>&nbsp;</p>
    <g:if test="${flash.message}">
        <div class="message"><g:message code="${flash.message}" args="${flash.args}"
                                        default="${flash.defaultMessage}"/></div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>
                <g:sortableColumn property="name" title="Name" titleKey="user.name"/>
                <g:sortableColumn property="account" title="gMail" titleKey="user.account"/>
                <th class="style7">Follow</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${userInstanceList}" status="i" var="userInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td>
                        <g:link action="show"
                                id="${userInstance.id}">${fieldValue(bean: userInstance, field: "name")}</g:link>
                    </td>
                    <td>${fieldValue(bean: userInstance, field: "account")}</td>

                    <g:if test="${!currentUser.usersFollowed.contains(userInstance)}">
                        <td><g:link action="showReports" id="${userInstance.id}">Follow</g:link></td>
                    </g:if>
                    <g:else>
                        <td>
                            <g:link action="unFollow" id="${userInstance.id}">Unfollow</g:link>
                        </td>
                    </g:else>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <span class="menuButton">
            <g:link class="create" action="create"><g:message code="user.new" default="New User"/></g:link>
        </span>
    </div>
</div>
</body>
</html>
