package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.domain.application.Application;

import java.util.function.Function;

interface ApplicationAction extends Function<Application, Application> {

}
