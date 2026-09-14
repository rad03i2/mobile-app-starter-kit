package dev.rad03i2.simplequiz

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

class MainActivity : Activity() {

    private data class Question(
        val category: String,
        val text: String,
        val answers: List<String>,
        val correctIndex: Int,
        val explanation: String
    )

    private enum class Screen { START, GAME, RESULT }

    private val questions = listOf(
        Question("معلومات عامة", "ما الكوكب الذي نعيش عليه؟", listOf("المريخ", "الأرض", "الزهرة", "عطارد"), 1, "الأرض هي الكوكب الثالث في المجموعة الشمسية وموطن الإنسان."),
        Question("رياضيات", "كم يساوي 9 × 7؟", listOf("56", "63", "72", "81"), 1, "تسعة في سبعة يساوي ثلاثة وستين."),
        Question("جغرافيا", "ما عاصمة المملكة الأردنية الهاشمية؟", listOf("عمّان", "العقبة", "إربد", "الزرقاء"), 0, "عمّان هي عاصمة الأردن وأكبر مدنه."),
        Question("علوم", "أي غاز تحتاجه معظم الكائنات الحية للتنفس؟", listOf("الهيليوم", "الهيدروجين", "الأكسجين", "النيون"), 2, "الأكسجين ضروري لعملية التنفس الخلوي لدى معظم الكائنات."),
        Question("لغة عربية", "ما جمع كلمة «كتاب»؟", listOf("كَتَبة", "مكاتب", "كتابات", "كُتُب"), 3, "جمع التكسير الصحيح لكلمة كتاب هو كُتُب."),
        Question("فلك", "كم عدد كواكب المجموعة الشمسية المعترف بها؟", listOf("سبعة", "ثمانية", "تسعة", "عشرة"), 1, "تضم المجموعة الشمسية ثمانية كواكب بعد تصنيف بلوتو ككوكب قزم."),
        Question("تاريخ", "في أي عام وصل الإنسان إلى سطح القمر لأول مرة؟", listOf("1959", "1965", "1969", "1975"), 2, "هبطت مهمة أبولو 11 على القمر عام 1969."),
        Question("فنون", "من رسم لوحة الموناليزا؟", listOf("بيكاسو", "فان غوخ", "رامبرانت", "ليوناردو دا فينشي"), 3, "رسم ليوناردو دا فينشي الموناليزا في عصر النهضة."),
        Question("رياضة", "كم لاعباً يشارك من كل فريق داخل ملعب كرة القدم؟", listOf("9", "10", "11", "12"), 2, "يبدأ كل فريق بأحد عشر لاعباً، بينهم حارس المرمى."),
        Question("كيمياء", "ما الرمز الكيميائي للذهب؟", listOf("Ag", "Au", "Fe", "Gd"), 1, "رمز الذهب Au مشتق من اسمه اللاتيني Aurum."),
        Question("حضارة عربية", "من العالم الذي يُعد من رواد علم البصريات؟", listOf("ابن الهيثم", "الخوارزمي", "ابن بطوطة", "الإدريسي"), 0, "قدّم ابن الهيثم إسهامات أساسية في علم الضوء والمنهج التجريبي."),
        Question("أحياء", "ما أكبر عضو في جسم الإنسان؟", listOf("الكبد", "الرئتان", "الجلد", "القلب"), 2, "الجلد هو أكبر أعضاء جسم الإنسان من حيث المساحة والكتلة."),
        Question("جغرافيا", "ما أكبر محيط على سطح الأرض؟", listOf("الأطلسي", "الهندي", "المتجمد الشمالي", "الهادئ"), 3, "المحيط الهادئ هو الأكبر مساحةً والأعمق على الأرض."),
        Question("فيزياء", "ما وحدة قياس شدة التيار الكهربائي؟", listOf("الفولت", "الأمبير", "الواط", "الأوم"), 1, "الأمبير هو وحدة قياس شدة التيار الكهربائي في النظام الدولي."),
        Question("طب وتاريخ", "ما اسم أشهر مؤلفات ابن سينا في الطب؟", listOf("الحاوي", "القانون في الطب", "المناظر", "التصريف"), 1, "كتاب «القانون في الطب» لابن سينا ظل مرجعاً طبياً لقرون.")
    )

    private val prizes = listOf(
        100, 200, 300, 500, 1_000,
        2_000, 4_000, 8_000, 16_000, 32_000,
        64_000, 125_000, 250_000, 500_000, 1_000_000
    )

    private val handler = Handler(Looper.getMainLooper())
    private val answerLabels = listOf("أ", "ب", "ج", "د")
    private var screen = Screen.START
    private var currentQuestion = 0
    private var remainingSeconds = QUESTION_TIME
    private var answered = false
    private var selectedAnswer = -1
    private var lastAnswerCorrect = false
    private var usedFifty = false
    private var usedPhone = false
    private var usedAudience = false
    private var hiddenAnswers = mutableSetOf<Int>()
    private var resultAmount = 0
    private var resultTitle = ""

    private lateinit var timerText: TextView
    private lateinit var timerBar: ProgressBar
    private lateinit var prizeText: TextView
    private lateinit var questionNumberText: TextView
    private lateinit var questionText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var nextButton: Button
    private lateinit var fiftyButton: Button
    private lateinit var phoneButton: Button
    private lateinit var audienceButton: Button
    private val answerButtons = mutableListOf<Button>()

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (screen != Screen.GAME || answered) return
            remainingSeconds--
            updateTimer()
            if (remainingSeconds <= 0) {
                revealAnswer(-1)
            } else {
                handler.postDelayed(this, 1_000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.navy_deep)
        window.navigationBarColor = getColor(R.color.navy_deep)
        restoreState(savedInstanceState)
        when (screen) {
            Screen.START -> showStartScreen()
            Screen.GAME -> showGameScreen()
            Screen.RESULT -> showResultScreen()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_SCREEN, screen.name)
        outState.putInt(KEY_QUESTION, currentQuestion)
        outState.putInt(KEY_TIME, remainingSeconds)
        outState.putBoolean(KEY_ANSWERED, answered)
        outState.putInt(KEY_SELECTED, selectedAnswer)
        outState.putBoolean(KEY_CORRECT, lastAnswerCorrect)
        outState.putBoolean(KEY_FIFTY, usedFifty)
        outState.putBoolean(KEY_PHONE, usedPhone)
        outState.putBoolean(KEY_AUDIENCE, usedAudience)
        outState.putIntArray(KEY_HIDDEN, hiddenAnswers.toIntArray())
        outState.putInt(KEY_RESULT_AMOUNT, resultAmount)
        outState.putString(KEY_RESULT_TITLE, resultTitle)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        stopTimer()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (screen == Screen.GAME && !answered && ::timerText.isInitialized) startTimer()
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (screen) {
            Screen.GAME -> confirmLeaveGame()
            Screen.RESULT -> showStartScreen()
            Screen.START -> super.onBackPressed()
        }
    }

    private fun restoreState(saved: Bundle?) {
        if (saved == null) return
        screen = runCatching { Screen.valueOf(saved.getString(KEY_SCREEN) ?: Screen.START.name) }.getOrDefault(Screen.START)
        currentQuestion = saved.getInt(KEY_QUESTION, 0).coerceIn(0, questions.lastIndex)
        remainingSeconds = saved.getInt(KEY_TIME, QUESTION_TIME).coerceIn(0, QUESTION_TIME)
        answered = saved.getBoolean(KEY_ANSWERED, false)
        selectedAnswer = saved.getInt(KEY_SELECTED, -1)
        lastAnswerCorrect = saved.getBoolean(KEY_CORRECT, false)
        usedFifty = saved.getBoolean(KEY_FIFTY, false)
        usedPhone = saved.getBoolean(KEY_PHONE, false)
        usedAudience = saved.getBoolean(KEY_AUDIENCE, false)
        hiddenAnswers = saved.getIntArray(KEY_HIDDEN)?.toMutableSet() ?: mutableSetOf()
        resultAmount = saved.getInt(KEY_RESULT_AMOUNT, 0)
        resultTitle = saved.getString(KEY_RESULT_TITLE).orEmpty()
    }

    private fun showStartScreen() {
        stopTimer()
        screen = Screen.START
        val content = verticalContainer(dp(26))
        content.addView(TextView(this).apply {
            text = "◆"
            textSize = 54f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.gold))
        }, fullWidth(dp(78)))
        content.addView(TextView(this).apply {
            text = getString(R.string.game_title)
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(0f, 0.9f)
        }, fullWidth(dp(112)))
        content.addView(TextView(this).apply {
            text = getString(R.string.game_subtitle)
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.gold_light))
        }, fullWidth(dp(48)))
        val rules = TextView(this).apply {
            text = getString(R.string.rules)
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.text_soft))
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = roundedPanel(R.color.navy_panel, R.color.gold_muted, 22f, 1)
        }
        content.addView(rules, fullWidth(dp(158)).apply { setMargins(0, dp(14), 0, dp(18)) })
        content.addView(actionButton(getString(R.string.start_game)).apply {
            contentDescription = getString(R.string.start_game_description)
            setOnClickListener { startNewGame() }
        }, fullWidth(dp(62)))
        val best = preferences().getInt(PREF_BEST, 0)
        content.addView(TextView(this).apply {
            text = getString(R.string.best_prize, money(best))
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.text_muted))
            setPadding(0, dp(18), 0, 0)
        }, fullWidth(dp(52)))
        setContentView(scroll(content))
    }

    private fun startNewGame() {
        screen = Screen.GAME
        currentQuestion = 0
        remainingSeconds = QUESTION_TIME
        answered = false
        selectedAnswer = -1
        lastAnswerCorrect = false
        usedFifty = false
        usedPhone = false
        usedAudience = false
        hiddenAnswers.clear()
        resultAmount = 0
        resultTitle = ""
        showGameScreen()
    }

    private fun showGameScreen() {
        screen = Screen.GAME
        answerButtons.clear()
        val content = verticalContainer(dp(16))
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        prizeText = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.gold))
        }
        val ladderButton = smallButton(getString(R.string.prize_ladder)).apply {
            contentDescription = getString(R.string.prize_ladder_description)
            setOnClickListener { showPrizeLadder() }
        }
        header.addView(prizeText, LinearLayout.LayoutParams(0, dp(54), 1f))
        header.addView(ladderButton, LinearLayout.LayoutParams(dp(118), dp(48)))
        content.addView(header, fullWidth(dp(58)))
        val timerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        timerText = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = roundedPanel(R.color.navy_panel, R.color.gold, 24f, 2)
        }
        timerBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = QUESTION_TIME
            progressTintList = colorState(R.color.gold)
            progressBackgroundTintList = colorState(R.color.navy_panel_light)
        }
        timerRow.addView(timerText, LinearLayout.LayoutParams(dp(64), dp(48)))
        timerRow.addView(timerBar, LinearLayout.LayoutParams(0, dp(20), 1f).apply { setMargins(dp(12), 0, 0, 0) })
        content.addView(timerRow, fullWidth(dp(62)))
        val helps = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        fiftyButton = lifelineButton("50:50", getString(R.string.fifty_description)) { useFiftyFifty() }
        phoneButton = lifelineButton("☎ ${getString(R.string.phone)}", getString(R.string.phone_description)) { usePhone() }
        audienceButton = lifelineButton("◉ ${getString(R.string.audience)}", getString(R.string.audience_description)) { useAudience() }
        helps.addView(fiftyButton, weightedButton())
        helps.addView(phoneButton, weightedButton())
        helps.addView(audienceButton, weightedButton())
        content.addView(helps, fullWidth(dp(58)).apply { setMargins(0, dp(4), 0, dp(10)) })
        questionNumberText = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.gold_light))
        }
        content.addView(questionNumberText, fullWidth(dp(34)))
        questionText = TextView(this).apply {
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(18), dp(14), dp(18), dp(14))
            background = roundedPanel(R.color.navy_panel, R.color.gold, 24f, 2)
        }
        content.addView(questionText, fullWidth(dp(118)).apply { setMargins(0, 0, 0, dp(12)) })
        repeat(4) { index ->
            val button = Button(this).apply {
                isAllCaps = false
                textSize = 17f
                gravity = Gravity.CENTER_VERTICAL
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                setTextColor(Color.WHITE)
                setPadding(dp(18), 0, dp(18), 0)
                background = answerBackground(R.color.navy_answer, R.color.gold_muted)
                setOnClickListener { selectAnswer(index) }
            }
            answerButtons += button
            content.addView(button, fullWidth(dp(60)).apply { setMargins(0, dp(5), 0, dp(5)) })
        }
        feedbackText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            visibility = View.GONE
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        content.addView(feedbackText, fullWidth(dp(90)))
        nextButton = actionButton(getString(R.string.next_question)).apply {
            visibility = View.GONE
            setOnClickListener { continueAfterAnswer() }
        }
        content.addView(nextButton, fullWidth(dp(58)).apply { setMargins(0, dp(6), 0, dp(8)) })
        content.addView(Button(this).apply {
            text = getString(R.string.walk_away)
            isAllCaps = false
            textSize = 15f
            setTextColor(getColor(R.color.text_soft))
            background = roundedPanel(R.color.navy_deep, R.color.text_muted, 18f, 1)
            setOnClickListener { confirmWalkAway() }
        }, fullWidth(dp(48)))
        setContentView(scroll(content))
        renderQuestion()
    }

    private fun renderQuestion() {
        val question = questions[currentQuestion]
        prizeText.text = getString(R.string.current_prize, money(prizes[currentQuestion]))
        questionNumberText.text = getString(R.string.question_position, currentQuestion + 1, questions.size, question.category)
        questionText.text = question.text
        updateTimer()
        updateLifelines()
        answerButtons.forEachIndexed { index, button ->
            button.text = "${answerLabels[index]}:  ${question.answers[index]}"
            button.visibility = if (index in hiddenAnswers) View.INVISIBLE else View.VISIBLE
            button.isEnabled = !answered
            button.alpha = if (answered) 0.9f else 1f
            button.background = answerBackground(R.color.navy_answer, R.color.gold_muted)
        }
        if (answered) {
            paintAnswerResult()
        } else {
            feedbackText.visibility = View.GONE
            nextButton.visibility = View.GONE
            startTimer()
        }
    }

    private fun selectAnswer(index: Int) {
        if (answered || index in hiddenAnswers) return
        selectedAnswer = index
        stopTimer()
        answerButtons.forEach { it.isEnabled = false }
        answerButtons[index].background = answerBackground(R.color.answer_selected, R.color.gold_light)
        answerButtons[index].animate().scaleX(1.025f).scaleY(1.025f).setDuration(180).withEndAction {
            answerButtons[index].animate().scaleX(1f).scaleY(1f).duration = 180
        }
        handler.postDelayed({ revealAnswer(index) }, 650)
    }

    private fun revealAnswer(index: Int) {
        if (answered) return
        stopTimer()
        answered = true
        selectedAnswer = index
        lastAnswerCorrect = index == questions[currentQuestion].correctIndex
        paintAnswerResult()
    }

    private fun paintAnswerResult() {
        val question = questions[currentQuestion]
        updateLifelines()
        answerButtons.forEachIndexed { index, button ->
            button.isEnabled = false
            button.visibility = if (index in hiddenAnswers) View.INVISIBLE else View.VISIBLE
            button.background = when {
                index == question.correctIndex -> answerBackground(R.color.correct, R.color.correct_light)
                index == selectedAnswer -> answerBackground(R.color.incorrect, R.color.incorrect_light)
                else -> answerBackground(R.color.navy_answer_disabled, R.color.navy_panel_light)
            }
        }
        feedbackText.apply {
            text = when {
                selectedAnswer < 0 -> getString(R.string.time_up, question.explanation)
                lastAnswerCorrect -> getString(R.string.correct_answer, question.explanation)
                else -> getString(R.string.wrong_answer, question.explanation)
            }
            background = roundedPanel(
                if (lastAnswerCorrect) R.color.correct_dark else R.color.incorrect_dark,
                if (lastAnswerCorrect) R.color.correct_light else R.color.incorrect_light,
                18f,
                1
            )
            visibility = View.VISIBLE
        }
        nextButton.apply {
            text = if (lastAnswerCorrect && currentQuestion < questions.lastIndex) getString(R.string.next_question) else getString(R.string.show_result)
            visibility = View.VISIBLE
        }
    }

    private fun continueAfterAnswer() {
        if (!lastAnswerCorrect) {
            finishGame(getString(R.string.game_over), guaranteedPrize())
            return
        }
        if (currentQuestion == questions.lastIndex) {
            finishGame(getString(R.string.million_winner), prizes.last())
            return
        }
        currentQuestion++
        remainingSeconds = QUESTION_TIME
        answered = false
        selectedAnswer = -1
        lastAnswerCorrect = false
        hiddenAnswers.clear()
        renderQuestion()
    }

    private fun useFiftyFifty() {
        if (answered || usedFifty) return
        usedFifty = true
        val correct = questions[currentQuestion].correctIndex
        hiddenAnswers = questions[currentQuestion].answers.indices.filter { it != correct }.take(2).toMutableSet()
        hiddenAnswers.forEach { answerButtons[it].visibility = View.INVISIBLE }
        updateLifelines()
    }

    private fun usePhone() {
        if (answered || usedPhone) return
        usedPhone = true
        updateLifelines()
        val question = questions[currentQuestion]
        val suggestion = "${answerLabels[question.correctIndex]}: ${question.answers[question.correctIndex]}"
        showPausedDialog(getString(R.string.phone_friend_title), getString(R.string.phone_friend_message, suggestion))
    }

    private fun useAudience() {
        if (answered || usedAudience) return
        usedAudience = true
        updateLifelines()
        val correct = questions[currentQuestion].correctIndex
        val percentages = mutableListOf(14, 9, 7, 10)
        percentages[correct] = 70
        val wrong = percentages.indices.filter { it != correct }
        percentages[wrong[0]] = 14
        percentages[wrong[1]] = 9
        percentages[wrong[2]] = 7
        val chart = percentages.mapIndexed { index, value ->
            "${answerLabels[index]}  ${"█".repeat(value / 5)} $value%"
        }.joinToString("\n\n")
        showPausedDialog(getString(R.string.audience_vote_title), chart)
    }

    private fun updateLifelines() {
        styleLifeline(fiftyButton, usedFifty)
        styleLifeline(phoneButton, usedPhone)
        styleLifeline(audienceButton, usedAudience)
    }

    private fun styleLifeline(button: Button, used: Boolean) {
        button.isEnabled = !used && !answered
        button.alpha = if (used) 0.38f else 1f
        button.background = roundedPanel(
            if (used) R.color.navy_answer_disabled else R.color.navy_panel,
            if (used) R.color.text_muted else R.color.gold,
            22f,
            1
        )
    }

    private fun showPrizeLadder() {
        val rows = prizes.indices.reversed().joinToString("\n") { index ->
            val marker = when {
                index == currentQuestion -> "◀"
                index == 4 || index == 9 || index == 14 -> "◆"
                else -> " "
            }
            "$marker  ${(index + 1).toString().padStart(2, '0')}     ${money(prizes[index])}"
        }
        showPausedDialog(getString(R.string.prize_ladder), rows)
    }

    private fun confirmWalkAway() {
        stopTimer()
        val amount = winningsBeforeCurrentQuestion()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.walk_away_title))
            .setMessage(getString(R.string.walk_away_message, money(amount)))
            .setNegativeButton(getString(R.string.keep_playing)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.confirm_walk_away)) { _, _ -> finishGame(getString(R.string.walked_away), amount) }
            .setOnDismissListener { if (screen == Screen.GAME && !answered) startTimer() }
            .show()
    }

    private fun confirmLeaveGame() {
        stopTimer()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.leave_title))
            .setMessage(getString(R.string.leave_message))
            .setNegativeButton(getString(R.string.stay)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.leave)) { _, _ -> showStartScreen() }
            .setOnDismissListener { if (screen == Screen.GAME && !answered) startTimer() }
            .show()
    }

    private fun showPausedDialog(title: String, message: String) {
        stopTimer()
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener { if (screen == Screen.GAME && !answered) startTimer() }
            .show()
    }

    private fun finishGame(title: String, amount: Int) {
        stopTimer()
        screen = Screen.RESULT
        resultTitle = title
        resultAmount = amount
        preferences().edit().putInt(PREF_BEST, maxOf(preferences().getInt(PREF_BEST, 0), amount)).apply()
        showResultScreen()
    }

    private fun showResultScreen() {
        stopTimer()
        screen = Screen.RESULT
        val content = verticalContainer(dp(26))
        content.addView(TextView(this).apply {
            text = if (resultAmount == prizes.last()) "★" else "◆"
            textSize = 76f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.gold))
        }, fullWidth(dp(108)))
        content.addView(TextView(this).apply {
            text = resultTitle.ifBlank { getString(R.string.game_over) }
            textSize = 30f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }, fullWidth(dp(76)))
        content.addView(TextView(this).apply {
            text = getString(R.string.you_won, money(resultAmount))
            textSize = 26f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.gold))
            setPadding(dp(14), dp(18), dp(14), dp(18))
            background = roundedPanel(R.color.navy_panel, R.color.gold, 24f, 2)
        }, fullWidth(dp(104)).apply { setMargins(0, dp(12), 0, dp(28)) })
        content.addView(actionButton(getString(R.string.play_again)).apply { setOnClickListener { startNewGame() } }, fullWidth(dp(62)))
        content.addView(Button(this).apply {
            text = getString(R.string.back_home)
            isAllCaps = false
            textSize = 17f
            setTextColor(getColor(R.color.text_soft))
            background = roundedPanel(R.color.navy_deep, R.color.gold_muted, 20f, 1)
            setOnClickListener { showStartScreen() }
        }, fullWidth(dp(56)).apply { setMargins(0, dp(12), 0, 0) })
        setContentView(scroll(content))
    }

    private fun startTimer() {
        stopTimer()
        if (screen == Screen.GAME && !answered && remainingSeconds > 0) handler.postDelayed(timerRunnable, 1_000)
    }

    private fun stopTimer() = handler.removeCallbacks(timerRunnable)

    private fun updateTimer() {
        if (!::timerText.isInitialized) return
        timerText.text = remainingSeconds.toString()
        timerBar.progress = remainingSeconds
        val urgent = remainingSeconds <= 10
        timerText.setTextColor(getColor(if (urgent) R.color.incorrect_light else R.color.white))
        timerBar.progressTintList = colorState(if (urgent) R.color.incorrect else R.color.gold)
    }

    private fun guaranteedPrize(): Int = when {
        currentQuestion >= 10 -> prizes[9]
        currentQuestion >= 5 -> prizes[4]
        else -> 0
    }

    private fun winningsBeforeCurrentQuestion(): Int = if (currentQuestion == 0) 0 else prizes[currentQuestion - 1]

    private fun money(amount: Int): String = "\$${NumberFormat.getIntegerInstance(Locale.US).format(amount)}"

    private fun preferences() = getSharedPreferences(PREFS, MODE_PRIVATE)

    private fun verticalContainer(horizontalPadding: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        setPadding(horizontalPadding, dp(24), horizontalPadding, dp(28))
        setBackgroundColor(getColor(R.color.navy_deep))
    }

    private fun scroll(child: View) = ScrollView(this).apply {
        isFillViewport = true
        setBackgroundColor(getColor(R.color.navy_deep))
        addView(child)
    }

    private fun actionButton(label: String) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 19f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(getColor(R.color.navy_deep))
        background = roundedPanel(R.color.gold, R.color.gold_light, 24f, 2)
    }

    private fun smallButton(label: String) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 14f
        setTextColor(getColor(R.color.gold_light))
        background = roundedPanel(R.color.navy_panel, R.color.gold_muted, 18f, 1)
    }

    private fun lifelineButton(label: String, description: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 13f
        contentDescription = description
        setTextColor(Color.WHITE)
        background = roundedPanel(R.color.navy_panel, R.color.gold, 22f, 1)
        setOnClickListener { action() }
    }

    private fun weightedButton() = LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(3), 0, dp(3), 0) }

    private fun answerBackground(fillColor: Int, strokeColor: Int) = roundedPanel(fillColor, strokeColor, 22f, 2)

    private fun roundedPanel(fillColor: Int, strokeColor: Int, radiusDp: Float, strokeDp: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(radiusDp.toInt()).toFloat()
        setColor(getColor(fillColor))
        setStroke(dp(strokeDp), getColor(strokeColor))
    }

    private fun colorState(color: Int) = android.content.res.ColorStateList.valueOf(getColor(color))

    private fun fullWidth(height: Int) = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val QUESTION_TIME = 30
        private const val PREFS = "millionaire_preferences"
        private const val PREF_BEST = "best_prize"
        private const val KEY_SCREEN = "screen"
        private const val KEY_QUESTION = "question"
        private const val KEY_TIME = "time"
        private const val KEY_ANSWERED = "answered"
        private const val KEY_SELECTED = "selected"
        private const val KEY_CORRECT = "correct"
        private const val KEY_FIFTY = "fifty"
        private const val KEY_PHONE = "phone"
        private const val KEY_AUDIENCE = "audience"
        private const val KEY_HIDDEN = "hidden"
        private const val KEY_RESULT_AMOUNT = "result_amount"
        private const val KEY_RESULT_TITLE = "result_title"
    }
}
