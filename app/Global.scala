import com.nilson.process.{GameData, BattleReportTemp$}
import play.api.GlobalSettings

object Global extends GlobalSettings {

  override def onStart(app : play.api.Application): Unit = {
    super.onStart(app)
//    BattleReport.processAll()
  }
}
