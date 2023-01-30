package com.example.kfsm

import com.example.kfsm.TurnstileState.LOCKED
import com.example.kfsm.TurnstileState.UNLOCKED
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

class TurnstileHandler(var _locked: Boolean) : Turnstile {
    val fsm = TurnstileFSM(this)

    fun currentState() = fsm.currentState()
    fun pass() = fsm.pass()
    fun coin() = fsm.coin()

    override val locked: Boolean
        get() = _locked

    override fun lock(): String {
        require(!locked) { "Expected to be unlocked " }
        _locked = true
        return ""
    }

    override fun unlock(): String {
        require(locked) { "Expected to be locked " }
        _locked = false
        return ""
    }

    override fun returnCoin(): String {
        return "Coin returned"
    }

    override fun alarm(): String {
        return "Alarm"
    }
}

@Controller
class TurnstileController {
    @GetMapping("/")
    fun index(): ModelAndView {
        val handler = TurnstileHandler(true)
        return result(handler, null)
    }

    @PostMapping(path = ["/event"], params = ["pass"])
    fun pass(@RequestParam("turnstileLocked") locked: Boolean): ModelAndView {
        val handler = TurnstileHandler(locked)
        return result(handler, handler.pass())
    }

    @PostMapping(path = ["/event"], params = ["coin"])
    fun coin(@RequestParam("turnstileLocked") locked: Boolean): ModelAndView {
        val handler = TurnstileHandler(locked)
        return result(handler, handler.coin())
    }

    private fun result(
        handler: TurnstileHandler,
        message: String?
    ): ModelAndView {
        val result = ModelAndView("index")
        val turnstileState = when (handler.currentState()) {
            LOCKED   -> "Locked"
            UNLOCKED -> "Unlocked"
        }
        TurnstileEvent.values().forEach { event ->
            result.addObject("${event.name.lowercase()}Disabled", !handler.fsm.allowed(event))
        }
        result.addObject("turnstileState", turnstileState)
        result.addObject("turnstileLocked", handler.locked)
        result.addObject("turnstileMessage", message)
        result.addObject("messageClass", if (message?.contains("Alarm") == true) "alarm" else "message")
        return result
    }
}
