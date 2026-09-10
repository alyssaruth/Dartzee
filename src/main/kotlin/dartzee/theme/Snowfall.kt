package dartzee.theme

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import javax.swing.SwingUtilities
import kotlin.random.Random

class Snowfall : JComponent() {

    data class Particle(val speed: Int, val x: Int, val y: Int, val color: Color)

    private var particles = listOf<Particle>()

    init {
        val r = Runnable {
            while (true) {
                Thread.sleep(200)
                SwingUtilities.invokeAndWait { doTick() }
            }
        }

        val t = Thread(r)
        t.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        g as Graphics2D

        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        particles.forEach { snow ->
            g.paint = snow.color
            g.drawLine(snow.x, snow.y, snow.x, snow.y)
        }
    }

    private fun doTick() {
        val updatedParticles = particles.map { snow ->
            val windEffect = Random.nextInt(-10, 10)
            val newY = snow.y + snow.speed
            val newX = snow.x + windEffect / 10
            snow.copy(x = newX, y = newY)
        }

        val newParticles =
            (0..width).mapNotNull { x ->
                val spawnSnow = Random.nextInt(20) >= 19
                if (spawnSnow) {
                    val baseColor = Color.WHITE
                    val offness = Random.nextInt(10)
                    val color =
                        Color(
                            baseColor.red - offness,
                            baseColor.green - offness,
                            baseColor.blue - offness,
                        )
                    val speed = if (Random.nextInt(10) >= 9) 2 else 1
                    Particle(speed, x, 0, color)
                } else {
                    null
                }
            }

        particles = updatedParticles + newParticles

        repaint()
    }
}
