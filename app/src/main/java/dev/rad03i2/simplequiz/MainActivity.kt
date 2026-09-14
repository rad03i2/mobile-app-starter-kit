package dev.rad03i2.simplequiz

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private data class Question(
        val text: String,
        val answers: List<String>,
        val correctIndex: Int,
        val explanation: String
    )

    private val questions = listOf(
        Question("ما عاصمة العراق؟", listOf("بغداد", "البصرة", "أربيل", "الموصل"), 0, "بغداد هي عاصمة جمهورية العراق."),
        Question("كم يساوي 5 + 3؟", listOf("6", "7", "8", "9"), 2, "خمسة زائد ثلاثة يساوي ثمانية."),
        Question("أي كوكب يُعرف بالكوكب الأحمر؟", listOf("الأرض", "المريخ", "الزهرة", "زحل"), 1, "يبدو المريخ أحمر بسبب أكاسيد الحديد على سطحه."),
        Question("ما اللغة المستخدمة غالباً لتطوير تطبيقات Android الحديثة؟", listOf("Kotlin", "HTML", "SQL", "CSS"), 0, "Kotlin لغة رسمية وحديثة لتطوير تطبيقات Android."),
        Question("كم عدد أيام الأسبوع؟", listOf("خمسة", "ستة", "سبعة", "ثمانية"), 2, "يتكون الأسبوع من سبعة أيام.")
    )

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var questionText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var nextButton: Button
    private val answerButtons = mutableListOf<Button>()

    private var currentQuestion = 0
    private var score = 0
    private var answered = false
    private var selectedAnswer = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_dark)
        buildScreen()

        currentQuestion = savedInstanceState?.getInt(KEY_QUESTION) ?: 0
        score = savedInstanceState?.getInt(KEY_SCORE) ?: 0
        answered = savedInstanceState?.getBoolean(KEY_ANSWERED) ?: false
        selectedAnswer = savedInstanceState?.getInt(KEY_SELECTED) ?: -1

        if (currentQuestion >= questions.size) {
            showResult()
        } else {
            showQuestion()
            if (answered && selectedAnswer >= 0) revealAnswer(selectedAnswer, updateScore = false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_QUESTION, currentQuestion)
        outState.putInt(KEY_SCORE, score)
        outState.putBoolean(KEY_ANSWERED, answered)
        outState.putInt(KEY_SELECTED, selectedAnswer)
        super.onSaveInstanceState(outState)
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            setBackgroundColor(getColor(R.color.background))
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        root.addView(title, fullWidth(dp(64)))

        progressText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.text_secondary))
        }
        root.addView(progressText, fullWidth(dp(36)))

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = questions.size
            progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
        }
        root.addView(progressBar, fullWidth(dp(20)))

        questionText = TextView(this).apply {
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.text_primary))
            setPadding(0, dp(18), 0, dp(18))
        }
        root.addView(questionText, fullWidth(dp(112)))

        repeat(4) { index ->
            val button = Button(this).apply {
                isAllCaps = false
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(getColor(R.color.option_default))
                setOnClickListener { revealAnswer(index, updateScore = true) }
            }
            answerButtons += button
            root.addView(button, fullWidth(dp(58)).apply { setMargins(0, dp(6), 0, dp(6)) })
        }

        feedbackText = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            visibility = View.INVISIBLE
            setPadding(dp(8), dp(14), dp(8), dp(10))
            setTextColor(getColor(R.color.text_primary))
        }
        root.addView(feedbackText, fullWidth(dp(80)))

        nextButton = Button(this).apply {
            text = getString(R.string.next)
            isAllCaps = false
            textSize = 18f
            isEnabled = false
            setOnClickListener { moveNext() }
        }
        root.addView(nextButton, fullWidth(dp(56)).apply { setMargins(0, dp(8), 0, 0) })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun showQuestion() {
        val question = questions[currentQuestion]
        progressText.text = getString(R.string.progress, currentQuestion + 1, questions.size)
        progressBar.progress = currentQuestion + 1
        questionText.text = question.text
        feedbackText.visibility = View.INVISIBLE
        nextButton.isEnabled = false
        nextButton.text = if (currentQuestion == questions.lastIndex) getString(R.string.show_result) else getString(R.string.next)

        answerButtons.forEachIndexed { index, button ->
            button.visibility = View.VISIBLE
            button.text = question.answers[index]
            button.isEnabled = true
            button.setTextColor(Color.WHITE)
            button.setBackgroundColor(getColor(R.color.option_default))
        }
    }

    private fun revealAnswer(selectedIndex: Int, updateScore: Boolean) {
        if (answered && updateScore) return
        val question = questions[currentQuestion]
        selectedAnswer = selectedIndex

        if (updateScore && selectedIndex == question.correctIndex) score++
        answered = true

        answerButtons.forEachIndexed { index, button ->
            button.isEnabled = false
            when {
                index == question.correctIndex -> button.setBackgroundColor(getColor(R.color.correct))
                index == selectedIndex -> button.setBackgroundColor(getColor(R.color.incorrect))
                else -> button.setBackgroundColor(getColor(R.color.option_disabled))
            }
        }

        feedbackText.text = if (selectedIndex == question.correctIndex) {
            getString(R.string.correct_answer, question.explanation)
        } else {
            getString(R.string.wrong_answer, question.explanation)
        }
        feedbackText.visibility = View.VISIBLE
        nextButton.isEnabled = true
    }

    private fun moveNext() {
        currentQuestion++
        answered = false
        selectedAnswer = -1
        if (currentQuestion >= questions.size) showResult() else showQuestion()
    }

    private fun showResult() {
        progressText.text = getString(R.string.finished)
        progressBar.progress = questions.size
        questionText.text = getString(R.string.final_score, score, questions.size)
        answerButtons.forEach { it.visibility = View.GONE }
        feedbackText.apply {
            text = resultMessage()
            visibility = View.VISIBLE
        }
        nextButton.apply {
            text = getString(R.string.try_again)
            isEnabled = true
            setOnClickListener { restartQuiz() }
        }
    }

    private fun resultMessage(): String = when {
        score == questions.size -> getString(R.string.result_perfect)
        score >= 3 -> getString(R.string.result_good)
        else -> getString(R.string.result_retry)
    }

    private fun restartQuiz() {
        currentQuestion = 0
        score = 0
        answered = false
        selectedAnswer = -1
        nextButton.setOnClickListener { moveNext() }
        showQuestion()
    }

    private fun fullWidth(height: Int) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        height
    )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val KEY_QUESTION = "question"
        private const val KEY_SCORE = "score"
        private const val KEY_ANSWERED = "answered"
        private const val KEY_SELECTED = "selected"
    }
}
