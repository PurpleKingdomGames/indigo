package indigo.shared.metrics

import scala.collection.mutable

trait Metrics {
  def record(m: Metric): Unit
  def recordForSpecificTime(m: Metric, time: Long): Unit
  def giveTime(): Long
  def giveMetrics: List[MetricWrapper]
}

object Metrics {

  val getNullInstance: Metrics =
    new Metrics {
      def record(m: Metric): Unit                            = ()
      def recordForSpecificTime(m: Metric, time: Long): Unit = ()
      def giveTime(): Long                                   = 1
      def giveMetrics: List[MetricWrapper]                   = Nil
    }

  private def metricsRecordingInstance(logReportIntervalMs: Int): Metrics =
    new Metrics {

      @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
      private val metrics: mutable.Queue[MetricWrapper] = new mutable.Queue[MetricWrapper]()

      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      private var lastReportTime: Long = System.currentTimeMillis()

      def record(m: Metric): Unit =
        recordForSpecificTime(m, giveTime())

      @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
      def recordForSpecificTime(m: Metric, time: Long): Unit = {
        metrics += MetricWrapper(m, time)

        m match {
          case FrameEndMetric if time >= lastReportTime + logReportIntervalMs =>
            lastReportTime = time
            MetricsLogReporter.report(metrics.dequeueAll(_ => true).toList)
          case _ => ()
        }
      }

      def giveTime(): Long = System.currentTimeMillis()

      def giveMetrics: List[MetricWrapper] =
        metrics.clone().dequeueAll(_ => true).toList

    }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var instance: Option[Metrics] = None

  def getInstance(enabled: Boolean, logReportIntervalMs: Int): Metrics =
    instance match {
      case Some(i) => i
      case None =>
        val i = if (enabled) {
          metricsRecordingInstance(logReportIntervalMs)
        } else {
          getNullInstance
        }

        instance = Some(i)
        i
    }

}
