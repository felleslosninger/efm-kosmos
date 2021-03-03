package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.domain.application.Application;

import java.util.function.Function;

interface ApplicationAction extends Function<Application, Application> {

}
