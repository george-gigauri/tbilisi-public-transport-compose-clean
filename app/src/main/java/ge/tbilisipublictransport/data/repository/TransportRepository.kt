package ge.tbilisipublictransport.data.repository

import ge.tbilisipublictransport.data.remote.api.TransportApi
import ge.tbilisipublictransport.domain.repository.ITransportRepository
import javax.inject.Inject

class TransportRepository @Inject constructor(
    private val api: TransportApi
): ITransportRepository {


}