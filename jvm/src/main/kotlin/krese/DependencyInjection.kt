package krese

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import krese.impl.*

val kodein = Kodein {
    bind<ApplicationConfiguration>() with singleton {  ConfigurationImpl() }
    bind<DatabaseConfiguration>() with singleton {  ConfigurationImpl() }
    bind<JWTReceiver>() with singleton {  JWTReceiverImpl(kodein) }
    bind<GetReceiver>() with singleton {  GetReceiverImpl(kodein) }
    bind<PostReceiver>() with singleton {  PostReceiverImpl(kodein) }
    bind<FileSystemWrapper>() with singleton {  FileSystemWrapperImpl(kodein) }
    bind<AuthVerifier>() with singleton {  AuthVerifierImpl(kodein) }
    bind<BusinessLogic>() with singleton {  BusinessLogicImpl(kodein) }
    bind<DatabaseEncapsulation>() with singleton {  DatabaseEncapsulationImpl(kodein) }
    bind<MailService>() with singleton {  MailServiceImpl(kodein) }
    bind<MailTemplater>() with singleton {  MailServiceImpl(kodein) }
}
