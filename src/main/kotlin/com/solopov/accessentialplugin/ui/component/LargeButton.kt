package com.solopov.accessentialplugin.ui.component

import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JButton
import javax.swing.SwingConstants
import javax.swing.border.AbstractBorder

@Suppress("MagicNumber")
class LargeButton(
    private val buttonText: String,
    private val tooltip: String = "",
    private val onClick: () -> Unit
) {
    private val buttonWidth = 60
    private val buttonHeight = 60

    fun create() =
        JButton().apply {
            text = buttonText
            toolTipText = tooltip
            name = buttonText

            font = Font(font.name, font.style, font.size)
            horizontalTextPosition = SwingConstants.CENTER
            verticalTextPosition = SwingConstants.BOTTOM
            foreground = Color.WHITE


            preferredSize = Dimension(buttonWidth, buttonHeight)
            maximumSize = Dimension(buttonWidth, buttonHeight)
            minimumSize = Dimension(buttonWidth, buttonHeight)
            margin = JBUI.insets(10)

            isContentAreaFilled = false
            isFocusPainted = false
            isBorderPainted = false
            background = Color(240, 240, 240)
            foreground = Color(60, 60, 60)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

            border = JBUI.Borders.customLine(Color.GRAY, 1)

            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseEntered(e: java.awt.event.MouseEvent?) {
                    background = Color(220, 220, 220)
                    isContentAreaFilled = true
                }

                override fun mouseExited(e: java.awt.event.MouseEvent?) {
                    background = Color(240, 240, 240)
                    isContentAreaFilled = false
                }
            })

            addActionListener { onClick() }
        }
}

class Button(
    private val buttonText: String,
    private val onClick: () -> Unit = {},
) {
    fun create(): JButton =
        JButton().apply {
            text = buttonText
            addActionListener {
                onClick()
            }
            font = Font("Arial", Font.BOLD, 16)

            maximumSize = Dimension(40, 15)

            background = Color(70, 130, 180)
            foreground = Color.WHITE

            border = RoundedBorder(15)

            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }

}

class RoundedBorder(private val radius: Int) : AbstractBorder() {
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val g2 = g as Graphics2D
        g2.color = Color.DARK_GRAY
        g2.stroke = BasicStroke(2f)
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius)
    }

    override fun getBorderInsets(c: Component): Insets {
        return Insets(radius + 1, radius + 1, radius + 1, radius + 1)
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        insets.set(radius + 1, radius + 1, radius + 1, radius + 1)
        return insets
    }
}
