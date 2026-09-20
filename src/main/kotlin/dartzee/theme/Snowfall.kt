package dartzee.theme

import dartzee.utils.DartsColour
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.SwingUtilities
import kotlin.random.Random

private const val MAX_FLAKE_CHANCE = 5

class Snowfall : AbstractMenuAnimation() {
    private val sync = Any()

    private val snowHeap: MutableMap<Int, Int> = mutableMapOf()
    var startTime: Long = -1

    data class Particle(
        val speed: Double,
        val windEffect: Int,
        val x: Int,
        val y: Double,
        val color: Color,
    )

    init {
        isOpaque = false
    }

    private var particles = listOf<Particle>()

    override fun start() {
        startTime = System.currentTimeMillis()

        val r = Runnable {
            while (true) {
                Thread.sleep(10)
                SwingUtilities.invokeAndWait {
                    synchronized(sync) {
                        doTick()
                    }
                }
            }
        }

        val t = Thread(r)
        t.start()
    }

    override fun reset() {
        synchronized(sync) {
            snowHeap.clear()
            particles = emptyList()
            startTime = System.currentTimeMillis()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g as Graphics2D

        g.color = DartsColour.TRANSPARENT
        g.fillRect(0, 0, width, height)

        particles.forEach { snow ->
            g.paint = snow.color
            g.drawLine(snow.x, snow.y.toInt(), snow.x, snow.y.toInt())
        }

        g.paint = Color.WHITE
        snowHeap.forEach { (x, snowflakes) ->
            (0 until snowflakes).forEach { index ->
                g.drawLine(x, height - index, x, height - index)
            }
        }
    }

    private fun doTick() {
        if (!active) {
            return
        }

        val timePassed = System.currentTimeMillis() - startTime
        val computedRate = (timePassed / 5000).toInt() + 1
        val flakeChance = minOf(MAX_FLAKE_CHANCE, computedRate)

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
            (-5..width + 5).mapNotNull { x ->
                val spawnSnow = Random.nextInt(1000) >= (1000 - flakeChance)
                if (spawnSnow) {
                    val offness = Random.nextInt(150)
                    val speed = Random.nextDouble(0.3, 0.7)
                    Particle(speed, 0, x, 0.0, offWhite(offness))
                } else {
                    null
                }
            }

        val newHeapParticles = updatedParticles.filter {
            (height - snowHeap.getSnowCount(it.x) == it.y.toInt() || it.y >= height)
        }
        val fallingParticles = updatedParticles - newHeapParticles

        particles = fallingParticles + newParticles

        newHeapParticles.forEach { snow ->
            if (snow.x in 0..width) {
                val current = snowHeap.getSnowCount(snow.x)
                snowHeap[snow.x] = current + 1
            }
        }

        smoothHeap()

        repaint()
    }

    private fun offWhite(offness: Int): Color {
        val actual = minOf(150, offness)
        return Color(255 - actual, 255 - actual, 255 - actual)
    }

    private fun smoothHeap() {
        val xValues = snowHeap.keys
        val peaks =
            xValues
                .filter { x ->
                    val height = snowHeap.getSnowCount(x)

                    val left = snowHeap.getSnowCount(x - 1)
                    val right = snowHeap.getSnowCount(x + 1)

                    height > left + 1 || height > right + 1
                }
                .filter { x -> x in 0 until width }
                .sortedByDescending { snowHeap.getValue(it) }

        val touched = mutableSetOf<Int>()
        peaks.forEach { x ->
            if (!touched.contains(x)) {
                touched.add(x)

                val current = snowHeap.getValue(x)
                snowHeap[x] = current - 1

                val left = snowHeap.getSnowCount(x - 1)
                val right = snowHeap.getSnowCount(x + 1)

                if (left > right) {
                    touched.add(x + 1)
                    snowHeap[x + 1] = right + 1
                } else {
                    touched.add(x - 1)
                    snowHeap[x - 1] = left + 1
                }
            }
        }
    }

    private fun MutableMap<Int, Int>.getSnowCount(x: Int) = getOrDefault(x, 0)
}
