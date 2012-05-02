/**
 * @author Leandro Larroulet
 * Date: Sep 13, 2010
 * Time: 13:05
 */

class InviteCoworkersStep2RequestHandler extends RequestHandler {


    def accepts(Conversation _conversation) {
      boolean accepted = _conversation.context.inviteCoworkers2
      return accepted
    }

    def doHandle(Conversation _conversation, ChatService _chatService) {
      _conversation.context.clear()

      User user =_conversation.actualRequest.user

      String actualMessage = _conversation.actualRequest.message

      int value
      try {
        value = Integer.parseInt(actualMessage)
      } catch (Exception e){
        // No valid integer number entered by user.
      }
      /**
        *  1: Ok
        *  2: Maybe
        *  3: No
        */

      if (value == 1){
        _conversation.responses << Response.build('sweet.InviteCoworkersStep2RequestHandlerYes')
        _conversation.context.inviteCoworkers3=true
      } else if (value == 2){
        _conversation.responses << Response.build('sweet.InviteCoworkersStep2RequestHandlerMaybe')
        user.remindToInviteCoworkers=true
        user.save()
        _conversation.context.clear()
      } else if (value == 3){
        _conversation.responses << Response.build('sweet.InviteCoworkersStep2RequestHandlerNo')
        user.remindToInviteCoworkers=false
        user.save()
        _conversation.context.clear()
      } else {
         _conversation.context.inviteCoworkers2=true
        _conversation.responses << Response.build('sweet.InviteCoworkersStep1RequestHandlerAsk1')
        _conversation.responses << Response.build('sweet.InviteCoworkersStep1RequestHandlerAsk2')
        _conversation.responses << Response.build('sweet.InviteCoworkersStep1RequestHandlerYes')
        _conversation.responses << Response.build('sweet.InviteCoworkersStep1RequestHandlerMaybe')
        _conversation.responses << Response.build('sweet.InviteCoworkersStep1RequestHandlerNo')
      }
      return
    }
}