package dartzee.theme

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import javax.swing.SwingUtilities
import kotlin.random.Random

class Snowfall : JComponent() {
    private val snowHeap: MutableMap<Int, List<Color>> = mutableMapOf()

    data class Particle(
        val speed: Int,
        val windEffect: Int,
        val x: Int,
        val y: Int,
        val color: Color,
    )

    private var particles = listOf<Particle>()

    init {
        val r = Runnable {
            while (true) {
                Thread.sleep(50)
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

        snowHeap.forEach { (x, snowflakes) ->
            snowflakes.forEachIndexed { index, color ->
                g.paint = color
                g.drawLine(x, height - index, x, height - index)
            }
        }
    }

    private fun doTick() {
        val updatedParticles = particles.map { snow ->
            val windEffect = Random.nextInt(-10, 11)
            val windImpact = windEffect / 10

            val newWindDirection =
                if (windImpact == 0) 0 else if (snow.windEffect == -windImpact) 0 else windImpact
            val newY = snow.y + snow.speed
            val newX = snow.x + newWindDirection
            snow.copy(x = newX, y = newY, windEffect = newWindDirection)
        }

        val newParticles =
            (0..width).mapNotNull { x ->
                val spawnSnow = Random.nextInt(100) >= 99
                if (spawnSnow) {
                    val baseColor = Color.WHITE
                    val offness = Random.nextInt(100)
                    val color =
                        Color(
                            baseColor.red - offness,
                            baseColor.green - offness,
                            baseColor.blue - offness,
                        )
                    val speed = if (Random.nextInt(10) >= 11) 2 else 1
                    Particle(speed, 0, x, 0, color)
                } else {
                    null
                }
            }

        val newHeapParticles = updatedParticles.filter {
            (height - snowHeap.getOrDefault(it.x, emptyList()).size) == it.y || it.y >= height
        }
        val fallingParticles = updatedParticles - newHeapParticles

        particles = fallingParticles + newParticles

        newHeapParticles.forEach {
            val current = snowHeap.getOrDefault(it.x, emptyList())
            snowHeap[it.x] = current + it.color
        }

        smoothHeap()

        repaint()
    }

    private fun smoothHeap() {
        val xValues = snowHeap.keys
        val peaks = xValues.filter { x ->
            val height = snowHeap.getSnowCount(x)

            val left = snowHeap.getSnowCount(x - 1)
            val right = snowHeap.getSnowCount(x + 1)

            height > left + 1 || height > right + 1
        }

        peaks.forEach { x ->
            val current = snowHeap.getValue(x)
            val flake = current.last()
            snowHeap[x] = current.dropLast(1)

            val left = snowHeap.getOrDefault(x - 1, emptyList())
            val right = snowHeap.getOrDefault(x + 1, emptyList())

            val direction = Random.nextInt(2)
            if (direction == 0 && right.size < current.size - 1) {
                snowHeap[x + 1] = right + flake
            } else {
                snowHeap[x - 1] = left + flake
            }
        }
    }

    private fun MutableMap<Int, List<Color>>.getSnowCount(x: Int) =
        getOrDefault(x, emptyList()).size
}
