package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.domain.application.Application;

import java.util.function.Function;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
interface ApplicationAction extends Function<Application, Application> {

}
