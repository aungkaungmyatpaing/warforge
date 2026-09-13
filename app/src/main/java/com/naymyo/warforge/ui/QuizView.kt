package com.naymyo.warforge.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.naymyo.warforge.R
import com.naymyo.warforge.data.Question

/**
 * A short multiple-choice run over one vehicle's own facts.
 *
 * A wrong answer is not a dead end: the explanation shows either way, because the point
 * of the quiz is to make the reader notice the thing they skimmed past, not to score
 * them.
 */
class QuizView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private var questions: List<Question> = emptyList()
    private var index = 0
    private var chosen: Int? = null
    private var score = 0

    init {
        orientation = VERTICAL
    }

    fun setQuestions(list: List<Question>) {
        questions = list
        index = 0
        chosen = null
        score = 0
        render()
    }

    private fun render() {
        removeAllViews()
        if (questions.isEmpty()) return
        if (index >= questions.size) {
            addView(label(context.getString(R.string.quiz_done), R.color.accent, 12f, mono = true))
            addView(
                label(
                    context.getString(R.string.quiz_score, score, questions.size),
                    R.color.ink, 20f, bold = true,
                )
            )
            addView(button(context.getString(R.string.restart)) { setQuestions(questions) })
            return
        }

        val q = questions[index]
        addView(
            label("QUESTION ${index + 1} / ${questions.size}", R.color.accent, 10f, mono = true)
        )
        addView(label(q.text, R.color.ink, 15f, bold = true).apply {
            setPadding(0, dp(6), 0, dp(10))
        })

        for ((i, answer) in q.answers.withIndex()) {
            addView(answerRow(q, i, answer))
        }

        chosen?.let { picked ->
            val right = picked == q.correct
            addView(
                label(
                    (if (right) "Correct. " else "Not quite. ") + q.because,
                    if (right) R.color.star_on else R.color.ink_dim, 12f,
                ).apply { setPadding(0, dp(10), 0, dp(4)) }
            )
            addView(
                button(
                    if (index == questions.size - 1) context.getString(R.string.quiz_done)
                    else context.getString(R.string.quiz_next)
                ) {
                    index++
                    chosen = null
                    render()
                }
            )
        }
    }

    private fun answerRow(q: Question, i: Int, answer: String): View {
        val picked = chosen
        val state = when {
            picked == null -> R.color.stroke
            i == q.correct -> R.color.star_on
            i == picked -> R.color.danger
            else -> R.color.stroke
        }
        return TextView(context).apply {
            text = answer
            textSize = 13f
            setTextColor(context.getColor(R.color.ink))
            setBackgroundResource(R.drawable.bg_ghost_button)
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                context.getColor(if (picked == null) R.color.bg_card else state)
            )
            setPadding(dp(14), dp(12), dp(14), dp(12))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                .apply { bottomMargin = dp(7) }
            if (picked == null) setOnClickListener {
                chosen = i
                if (i == q.correct) score++
                render()
            }
        }
    }

    private fun label(
        text: String, color: Int, size: Float, bold: Boolean = false, mono: Boolean = false,
    ) = TextView(context).apply {
        this.text = text
        textSize = size
        setTextColor(context.getColor(color))
        if (bold) setTypeface(null, Typeface.BOLD)
        if (mono) typeface = Typeface.MONOSPACE
        if (mono) letterSpacing = 0.12f
        setLineSpacing(dp(3).toFloat(), 1f)
    }

    private fun button(text: String, onClick: () -> Unit) = TextView(context).apply {
        this.text = text
        textSize = 13f
        setTextColor(0xFF1A1206.toInt())
        setTypeface(null, Typeface.BOLD)
        gravity = android.view.Gravity.CENTER
        setBackgroundResource(R.drawable.bg_primary_button)
        setPadding(dp(14), dp(13), dp(14), dp(13))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            .apply { topMargin = dp(10) }
        setOnClickListener { onClick() }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
