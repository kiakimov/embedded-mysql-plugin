package uk.co.mruoc.mysql

import com.mysql.cj.jdbc.exceptions.CommunicationsException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import java.sql.DriverManager

import static com.wix.mysql.distribution.Version.v5_6_23
import static org.assertj.core.api.Assertions.assertThat

class StartStopEmbeddedMysqlTaskTest {

    private static final def DATABASE_NAME = "databaseName"
    private static final def PORT = 3307
    private static final def URL = "jdbc:mysql://localhost:" + PORT + "/" + DATABASE_NAME
    private static final def USERNAME = "user"
    private static final def PASSWORD = ""
    private static final def VERSION = v5_6_23.name()

    private def project = ProjectBuilder.builder().build()

    @Test
    void shouldStartAndStopMysql() {
        configureExtension()

        executeStartTask()
        assertThat(mysqlRunning()).isTrue()

        executeStopTask()
        assertThat(mysqlRunning()).isFalse()
    }

    @Test
    void shouldStartSpecifiedMysqlVersion() {
        configureExtension()

        executeStartTask()
        assertThat(mysqlVersion).isEqualTo(format(VERSION))

        executeStopTask()
    }

    @Test
    void shouldDoNothingIfStopCalledWithoutStart() {
        configureExtension()
        executeStopTask()
    }

    private configureExtension() {
        def extension = getExtension()
        extension.url = URL
        extension.username = USERNAME
        extension.password = PASSWORD
        extension.version = VERSION
    }

    private getExtension() {
        project.extensions.create('embeddedMysql', EmbeddedMysqlExtension)
    }

    private executeStartTask() {
        def task = project.task('startEmbeddedMysql', type: StartEmbeddedMysqlTask)
        task.execute()
    }

    private executeStopTask() {
        def task = project.task('stopEmbeddedMysql', type: StopEmbeddedMysqlTask)
        task.execute()
    }

    private mysqlRunning() {
        try {
            def connection = getConnection()
            try {
                return connection.isValid(1)
            } finally {
                connection.close()
            }
        } catch (CommunicationsException e) {
            return false
        }
    }

    private getMysqlVersion() {
        def connection = getConnection()
        try {
            def meta = connection.getMetaData()
            return meta.getDatabaseProductVersion()
        } finally {
            connection.close()
        }
    }

    private getConnection() {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD)
    }

    private format(String version) {
        version = version.replaceAll("_", ".")
        version = version.replaceAll("v", "")
        return version
    }

}
