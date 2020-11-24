package restdoc.web.repository

import org.springframework.stereotype.Repository
import restdoc.web.base.mongo.BaseRepository
import restdoc.web.model.DubboDocument
import restdoc.web.model.RestWebDocument

@Repository
interface DubboDocumentRepository : BaseRepository<DubboDocument, String>