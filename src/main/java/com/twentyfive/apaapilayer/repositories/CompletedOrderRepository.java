package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CompletedOrderAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CompletedOrderRepository extends MongoRepository<CompletedOrderAPA,String> {

    List<CompletedOrderAPA> findAllByOrderByCreatedDateDesc();
    List<CompletedOrderAPA> findByCustomerIdOrderByCreatedDateDesc(String customerId);
    Page<CompletedOrderAPA> findOrdersByCustomerIdOrderByCreatedDateDesc(String customerId, Pageable pageable);
    List<CompletedOrderAPA> findAllByPickupDateAndStatus(LocalDate date, OrderStatus status);

    @Aggregation(pipeline = {
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",
            "{ $group: { _id: null, total: { $sum: '$totalPrice' } } }"
    })
    Optional<Double> calculateTotalPriceByPickupDateAndStatus(LocalDate pickupDate, OrderStatus status);

    @Query(value = "{ 'pickupDate': ?0, 'status': ?1 }", count = true)
    long countByPickupDateAndStatus(LocalDate pickupDate, String status);

    @Aggregation(pipeline = {
            "{ $match: { 'pickupDate': ?0, 'status': ?1, 'customerId': { $ne: null } } }",
            "{ $group: { _id: '$customerId' } }",
            "{ $project: { _id: 0, customerId: '$_id' } }"
    })
    List<String> findDistinctCustomerIdsByPickupDateAndStatus(LocalDate pickupDate, OrderStatus status);



    @Aggregation(pipeline = {
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",
            "{ $project: { totalQuantity: { $add: [ { $sum: '$productsInPurchase.quantity' }, { $sum: '$bundlesInPurchase.quantity' } ] } } }",
            "{ $group: { _id: null, totalQuantity: { $sum: '$totalQuantity' } } }"
    })
    Optional<Long> sumQuantitiesByPickupDateAndStatus(LocalDate pickupDate, OrderStatus status);

    @Aggregation(pipeline = {
            "{ $match: { 'pickupDate': ?0, 'status': ?1, 'productsInPurchase.id': ?2 } }",
            "{ $unwind: '$productsInPurchase' }",
            "{ $match: { 'productsInPurchase.id': ?2 } }",
            "{ $group: { _id: null, totalQuantity: { $sum: '$productsInPurchase.quantity' } } }"
    })
    Optional<Long> sumQuantityByPickupDateStatusAndProductId(
            LocalDate pickupDate,
            OrderStatus status,
            String productId
    );

    @Aggregation(pipeline = {
            "{ $match: { 'pickupDate': ?0, 'status': ?1, 'productsInPurchase.id': ?2 } }",
            "{ $unwind: '$productsInPurchase' }",
            "{ $match: { 'productsInPurchase.id': ?2 } }",
            "{ $group: { _id: null, totalPrice: { $sum: '$totalPrice' } } }"
    })
    Optional<Double> sumTotalPriceByPickupDateStatusAndProductId(
            LocalDate pickupDate,
            OrderStatus status,
            String productId
    );

    @Aggregation(pipeline = {
            // Filtro per pickupDate e status
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",

            // Espande la lista productsInPurchase
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei prodotti dalla collezione 'products'
            "{ $lookup: { " +
                    "'from': 'products', " +
                    "'localField': 'productsInPurchase._id', " +
                    "'foreignField': '_id', " +
                    "'as': 'productDetails' } }",

            // Espande i dettagli dei prodotti
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei bundles dalla collezione 'trays'
            "{ $lookup: { " +
                    "'from': 'trays', " +
                    "'localField': 'bundlesInPurchase._id', " +
                    "'foreignField': '_id', " +
                    "'as': 'bundleDetails' } }",

            // Espande i dettagli dei bundles
            "{ $unwind: { path: '$bundleDetails', preserveNullAndEmptyArrays: true } }",

            // Calcola il totalPrice per productsInPurchase
            "{ $project: { " +
                    "productTotalPrice: { $cond: { " +
                    "if: { $eq: ['$productDetails.categoryId', ?2] }, " +
                    "then: '$productsInPurchase.totalPrice', " +
                    "else: 0 " +
                    "} }, " +
                    "bundleTotalPrice: { $cond: { " +
                    "if: { $eq: ['$bundleDetails.categoryId', ?2] }, " +
                    "then: '$bundlesInPurchase.totalPrice', " +
                    "else: 0 " +
                    "} } " +
                    "} }",

            // Raggruppa e somma i prezzi totali
            "{ $group: { _id: null, totalPrice: { $sum: { $add: ['$productTotalPrice', '$bundleTotalPrice'] } } } }",

            // Proietta il risultato finale
            "{ $project: { totalPrice: { $ifNull: ['$totalPrice', 0] } } }"
    })
    Optional<Double> sumTotalPriceByCategoryId(
            LocalDate pickupDate,
            OrderStatus status,
            String categoryId
    );

    @Aggregation(pipeline = {
            // Filtro per pickupDate e status
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",

            // Espande la lista productsInPurchase
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei prodotti dalla collezione 'products'
            "{ $lookup: { " +
                    "'from': 'products', " +
                    "'localField': 'productsInPurchase._id', " +
                    "'foreignField': '_id', " +
                    "'as': 'productDetails' } }",

            // Espande i dettagli dei prodotti
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei bundle dalla collezione 'trays'
            "{ $lookup: { " +
                    "'from': 'trays', " +
                    "'localField': 'bundlesInPurchase._id', " +
                    "'foreignField': '_id', " +
                    "'as': 'bundleDetails' } }",

            // Espande i dettagli dei bundle
            "{ $unwind: { path: '$bundleDetails', preserveNullAndEmptyArrays: true } }",

            // Calcola la quantità combinata per productsInPurchase e bundlesInPurchase
            "{ $project: { " +
                    "quantity: { $cond: { " +
                    "if: { $and: [ { $ne: ['$productDetails', null] }, { $eq: ['$productDetails.categoryId', ?2] } ] }, " +
                    "then: '$productsInPurchase.quantity', " +
                    "else: { $cond: { " +
                    "if: { $and: [ { $ne: ['$bundleDetails', null] }, { $eq: ['$bundleDetails.categoryId', ?2] } ] }, " +
                    "then: '$bundlesInPurchase.quantity', " +
                    "else: 0 " +
                    "} } " +
                    "} }, " +
                    "categoryId: { $ifNull: ['$productDetails.categoryId', '$bundleDetails.categoryId'] } " +
                    "} }",

            // Raggruppa e somma le quantità
            "{ $group: { _id: null, totalQuantity: { $sum: '$quantity' } } }",

            // Usa $ifNull per restituire 0 se totalQuantity è null
            "{ $project: { totalQuantity: { $ifNull: ['$totalQuantity', 0] } } }"
    })
    Optional<Long> sumQuantityByCategoryId(
            LocalDate pickupDate,
            OrderStatus status,
            String categoryId
    );

    @Aggregation(pipeline = {
            // 1. Filtro ordini per stato e data
            "{ $match: { status: ?1, pickupDate: ?2 } }",

            // 2. Estrai gli _id dei prodotti acquistati
            "{ $project: { " +
                    "productIds: { $map: { input: '$productsInPurchase', as: 'product', in: '$$product._id' } } " +
                    "} }",

            // 3. Join con la collezione products
            "{ $lookup: { from: 'products', localField: 'productIds', foreignField: '_id', as: 'products' } }",

            // 4. Filtro per categoria
            "{ $project: { " +
                    "filteredProductIds: { $filter: { input: '$products', as: 'product', cond: { $eq: ['$$product.categoryId', ?0] } } } " +
                    "} }",

            // 5. Estrai solo gli _id
            "{ $project: { " +
                    "finalIds: { $map: { input: '$filteredProductIds', as: 'product', in: '$$product._id' } } " +
                    "} }",

            // 6. Flatten array in documenti singoli
            "{ $unwind: '$finalIds' }",

            // 7. Proiezione finale
            "{ $project: { _id: '$finalIds' } }"
    })
    Set<String> findProductIdsByCategoryAndStatus(String categoryId, OrderStatus status, LocalDate pickupDate);




    @Aggregation(pipeline = {
            // Filtra per pickupDate, OrderStatus e productId (per ProductInPurchase o BundleInPurchase)
            "{ $match: { 'pickupDate': ?0, 'status': ?1, $or: [" +
                    "{ 'productsInPurchase._id': ?2 }, " +
                    "{ 'bundlesInPurchase._id': ?2 } " +
                    "] } }",

            // Espande la lista productsInPurchase
            "{ $unwind: '$productsInPurchase' }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Filtra solo il productId specifico nelle due liste
            "{ $match: { $or: [" +
                    "{ 'productsInPurchase._id': ?2 }, " +
                    "{ 'bundlesInPurchase._id': ?2 } " +
                    "] } }",

            // Proietta solo le quantità dei prodotti e dei bundle, garantendo che siano numeri
            "{ $project: { " +
                    "productQuantity: { $ifNull: ['$productsInPurchase.quantity', 0] }, " +
                    "bundleQuantity: { $ifNull: ['$bundlesInPurchase.quantity', 0] } " +
                    "} }",

            // Raggruppa i risultati e somma le quantità
            "{ $group: { _id: null, totalQuantity: { $sum: { $add: ['$productQuantity', '$bundleQuantity'] } } } }"
    })
    Optional<Long> sumQuantityByProductId(
            LocalDate date,
            OrderStatus status,
            String productId
    );


    @Aggregation(pipeline = {
            // Filtra per pickupDate, OrderStatus e productId (per ProductInPurchase o BundleInPurchase)
            "{ $match: { 'pickupDate': ?0, 'status': ?1, $or: [" +
                    "{ 'productsInPurchase._id': ?2 }, " +  // Filtro per productId nei productsInPurchase
                    "{ 'bundlesInPurchase._id': ?2 } " +    // Filtro per productId nei bundlesInPurchase
                    "] } }",

            // Espande la lista productsInPurchase
            "{ $unwind: '$productsInPurchase' }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Filtra solo il productId specifico nelle due liste
            "{ $match: { $or: [" +
                    "{ 'productsInPurchase._id': ?2 }, " +  // Filtro per productId nei productsInPurchase
                    "{ 'bundlesInPurchase._id': ?2 } " +    // Filtro per productId nei bundlesInPurchase
                    "] } }",

            // Proietta solo i totalPrice dei prodotti e dei bundle, garantendo che siano numeri
            "{ $project: { " +
                    "productTotalPrice: { $ifNull: ['$productsInPurchase.totalPrice', 0] }, " +
                    "bundleTotalPrice: { $ifNull: ['$bundlesInPurchase.totalPrice', 0] } " +
                    "} }",

            // Raggruppa i risultati e somma i totalPrice
            "{ $group: { _id: null, totalQuantity: { $sum: { $add: ['$productTotalPrice', '$bundleTotalPrice'] } } } }"
    })
    Optional<Double> sumTotalPriceByProductId(
            LocalDate date,
            OrderStatus status,
            String productId
    );


    @Aggregation(pipeline = {
            // Filtra per pickupDate, OrderStatus e productId (per ProductInPurchase o BundleInPurchase)
            "{ $match: { 'pickupDate': ?0, 'status': ?1, $or: [" +
                    "{ 'productsInPurchase._id': ?2 }, " +  // Filtro per productId nei productsInPurchase
                    "{ 'bundlesInPurchase._id': ?2 } " +    // Filtro per productId nei bundlesInPurchase
                    "] } }",

            // Espande la lista productsInPurchase
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Filtra per productId nelle due liste, solo se presente
            "{ $match: { $or: [" +
                    "{ 'productsInPurchase._id': ?2 }, " +  // Filtro per productId nei productsInPurchase
                    "{ 'bundlesInPurchase._id': ?2 } " +    // Filtro per productId nei bundlesInPurchase
                    "] } }",

            // Proietta solo il peso dei prodotti e dei bundle
            "{ $project: { " +
                    "productWeight: { $ifNull: ['$productsInPurchase.weight', 0] }, " +
                    "bundleWeight: { $ifNull: ['$bundlesInPurchase.weight', 0] } " + // Assicurati che 'weight' sia il campo giusto
                    "} }",

            // Raggruppa i risultati e somma i pesi totali
            "{ $group: { _id: null, totalWeight: { $sum: { $add: ['$productWeight', '$bundleWeight'] } } } }"
    })
    Optional<Double> sumTotalWeightByProductId(
            LocalDate date,
            OrderStatus status,
            String productId
    );


    @Aggregation(pipeline = {
            // 1. Filtro per data e stato
            "{ '$match': { 'pickupDate': ?0, 'status': ?1 } }",

            // 2. Lookup su products per ogni _id in productsInPurchase
            "{ '$lookup': { 'from': 'products', 'localField': 'productsInPurchase._id', 'foreignField': '_id', 'as': 'productDetails' } }",

            // 3. Estrai tutti i categoryId dei prodotti acquistati
            "{ '$project': { " +
                    "'productCategoryIds': { '$map': { 'input': '$productDetails', 'as': 'product', 'in': '$$product.categoryId' } }" +
                    "} }",

            // 4. Unwind per ottenere una riga per ogni categoryId
            "{ '$unwind': '$productCategoryIds' }",

            // 5. Group per rimuovere duplicati
            "{ '$group': { '_id': '$productCategoryIds' } }",

            // 6. Proiezione finale pulita
            "{ '$project': { '_id': 0, 'categoryId': '$_id' } }"
    })
    List<String> findDistinctCategoryIdsByDateAndStatus(LocalDate date, OrderStatus status);


    @Aggregation(pipeline = {
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",
            "{ $lookup: { from: 'products', localField: 'bundlesInPurchase.weightedProducts._id', foreignField: '_id', as: 'bundleProductDetails' } }",
            "{ $unwind: { path: '$bundleProductDetails', preserveNullAndEmptyArrays: true } }",
            "{ $project: { ingredientIds: { $concatArrays: [ { $ifNull: ['$productDetails.ingredientIds', []] }, { $ifNull: ['$bundleProductDetails.ingredientIds', []] } ] } } }",
            "{ $unwind: { path: '$ingredientIds', preserveNullAndEmptyArrays: true } }",
            "{ $group: { _id: '$ingredientIds' } }",  // Raggruppa per ottenere valori unici
            "{ $project: { _id: 0, ingredientId: '$_id' } }"  // Proietta solo gli ID
    })
    List<String> findUniqueIngredientIds(LocalDate date, OrderStatus status);




    @Aggregation(pipeline = {
            // Filtra per pickupDate e OrderStatus
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",

            // Espande productsInPurchase
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",

            // Lookup su products per ottenere ingredientIds dai productsInPurchase
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",

            // Espande i dettagli del prodotto
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",

            // Calcola il numero di ingredienti per productsInPurchase
            "{ $addFields: { productIngredientCount: { $multiply: [ { $size: { $ifNull: ['$productDetails.ingredientIds', []] } }, { $ifNull: ['$productsInPurchase.quantity', 1] } ] } } }",

            // Espande bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",

            // Lookup su products per ottenere ingredientIds dai weightedProducts nei bundle
            "{ $lookup: { from: 'products', localField: 'bundlesInPurchase.weightedProducts._id', foreignField: '_id', as: 'bundleProductDetails' } }",

            // Espande i dettagli del prodotto nei bundle
            "{ $unwind: { path: '$bundleProductDetails', preserveNullAndEmptyArrays: true } }",

            // Calcola il numero di ingredienti per bundlesInPurchase
            "{ $addFields: { bundleIngredientCount: { $multiply: [ { $size: { $ifNull: ['$bundleProductDetails.ingredientIds', []] } }, { $ifNull: ['$bundlesInPurchase.quantity', 1] } ] } } }",

            // Somma gli ingredienti di entrambi i tipi di acquisti
            "{ $group: { _id: null, totalIngredients: { $sum: { $add: [ '$productIngredientCount', '$bundleIngredientCount' ] } } } }",

            // Proietta il risultato finale
            "{ $project: { _id: 0, totalIngredients: 1 } }"
    })
    Optional<Long> calculateTotalIngredients(LocalDate date, OrderStatus status);

    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$productDetails.ingredientIds', preserveNullAndEmptyArrays: true } }",

            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",
            "{ $lookup: { from: 'products', localField: 'bundlesInPurchase.weightedProducts._id', foreignField: '_id', as: 'bundleProductDetails' } }",
            "{ $unwind: { path: '$bundleProductDetails', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundleProductDetails.ingredientIds', preserveNullAndEmptyArrays: true } }",

            // Filtra ingredientIds per rimuovere null
            "{ $match: { 'productDetails.ingredientIds': { $ne: null } } }",
            "{ $match: { 'bundleProductDetails.ingredientIds': { $ne: null } } }",

            "{ $group: { _id: '$productDetails.ingredientIds' } }",
            "{ $group: { _id: '$_id', ingredientId: { $first: '$_id' } } }",
            "{ $project: { _id: 0, ingredientId: 1 } }"
    })
    List<String> findDistinctIngredientIds(LocalDate date, OrderStatus status);


    @Aggregation(pipeline = {
            // 1. Filtra per pickupDate e status
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // 2. Proietta gli ID dei prodotti da productsInPurchase e dai bundlesInPurchase.weightedProducts
            "{ $project: { " +
                    "productsIds: { $map: { input: { $ifNull: [ \"$productsInPurchase\", [] ] }, as: \"p\", in: \"$$p._id\" } }, " +
                    "bundlesProductIds: { " +
                    "  $reduce: { " +
                    "    input: { $ifNull: [ \"$bundlesInPurchase\", [] ] }, " +
                    "    initialValue: [], " +
                    "    in: { $setUnion: [ \"$$value\", " +
                    "          { $map: { input: { $ifNull: [ \"$$this.weightedProducts\", [] ] }, as: \"bp\", in: \"$$bp._id\" } } " +
                    "        ] } " +
                    "  } " +
                    "} " +
                    "} }",

            // 3. Unisce in un unico array tutti gli ID
            "{ $project: { allProductIds: { $setUnion: [ \"$productsIds\", \"$bundlesProductIds\" ] } } }",

            // 4. Lookup nella collezione products per ottenere i documenti relativi
            "{ $lookup: { from: \"products\", localField: \"allProductIds\", foreignField: \"_id\", as: \"products\" } }",

            // 5. Unwind per iterare su ogni prodotto trovato
            "{ $unwind: \"$products\" }",

            // 6. Unwind per iterare su ogni ingredientId presente in ogni prodotto
            "{ $unwind: \"$products.ingredientIds\" }",

            // 7. Lookup nella collezione ingredients: convertiamo il campo ingredientIds (stringa) in ObjectId per farlo combaciare con _id
            "{ $lookup: { " +
                    "from: \"ingredients\", " +
                    "let: { ingId: { $toObjectId: \"$products.ingredientIds\" } }, " +
                    "pipeline: [ " +
                    "{ $match: { $expr: { $eq: [ \"$_id\", \"$$ingId\" ] } } } " +
                    "], " +
                    "as: \"ingredientDetails\" " +
                    "} }",

            // 8. Unwind l’array ingredientDetails (in genere contiene un solo elemento se la conversione è andata a buon fine)
            "{ $unwind: \"$ingredientDetails\" }",

            // 9. Raggruppa per ottenere i categoryId distinti
            "{ $group: { _id: \"$ingredientDetails.categoryId\" } }",

            // 10. Proietta il campo categoryId
            "{ $project: { _id: 0, categoryId: \"$_id\" } }"
    })
    List<String> findDistinctIngredientCategoryIds(LocalDate pickupDate, OrderStatus status);

    @Aggregation(pipeline = {
            // 1. Filtra per pickupDate e status
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // 2. Proietta gli ID dei prodotti da productsInPurchase e dai bundlesInPurchase.weightedProducts
            "{ $project: { " +
                    "productsIds: { $map: { input: { $ifNull: [ \"$productsInPurchase\", [] ] }, as: \"p\", in: \"$$p._id\" } }, " +
                    "bundlesProductIds: { " +
                    "$reduce: { " +
                    "input: { $ifNull: [ \"$bundlesInPurchase\", [] ] }, " +
                    "initialValue: [], " +
                    "in: { $setUnion: [ \"$$value\", " +
                    "{ $map: { input: { $ifNull: [ \"$$this.weightedProducts\", [] ] }, as: \"bp\", in: \"$$bp._id\" } } " +
                    "] } " +
                    "} " +
                    "} " +
                    "} }",

            // 3. Unisce in un unico array tutti gli ID
            "{ $project: { allProductIds: { $setUnion: [ \"$productsIds\", \"$bundlesProductIds\" ] } } }",

            // 4. Lookup in products per ottenere i documenti relativi agli ID
            "{ $lookup: { from: \"products\", localField: \"allProductIds\", foreignField: \"_id\", as: \"products\" } }",
            "{ $unwind: \"$products\" }",

            // 5. Per ogni prodotto, esplodi l'array degli ingredientIds
            "{ $unwind: \"$products.ingredientIds\" }",

            // 6. Lookup in ingredients per ottenere i dettagli dell'ingrediente
            //    Si usa $toObjectId perché in products.ingredientIds il valore potrebbe essere una stringa mentre in ingredients gli _id sono ObjectId.
            "{ $lookup: { " +
                    "from: \"ingredients\", " +
                    "let: { ingId: { $toObjectId: \"$products.ingredientIds\" } }, " +
                    "pipeline: [ { $match: { $expr: { $eq: [ \"$_id\", \"$$ingId\" ] } } } ], " +
                    "as: \"ingredientDetails\" " +
                    "} }",
            "{ $unwind: \"$ingredientDetails\" }",

            // 7. Filtra gli ingredienti in base alla categoria specificata
            "{ $match: { \"ingredientDetails.categoryId\": ?2 } }",

            // 8. Conta ogni occorrenza (ogni documento rappresenta un ingrediente usato)
            "{ $group: { _id: null, ingredientCount: { $sum: 1 } } }",

            // 9. Proietta il risultato finale
            "{ $project: { _id: 0, ingredientCount: 1 } }"
    })
    Optional<Long> countIngredientsByCategory(LocalDate pickupDate, OrderStatus status, String categoryId);


    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // Proietta gli ID dei prodotti da productsInPurchase e dai bundlesInPurchase.weightedProducts
            "{ $project: { " +
                    "productsIds: { $map: { input: { $ifNull: [ \"$productsInPurchase\", [] ] }, as: \"p\", in: \"$$p._id\" } }, " +
                    "bundlesProductIds: { " +
                    "$reduce: { " +
                    "input: { $ifNull: [ \"$bundlesInPurchase\", [] ] }, " +
                    "initialValue: [], " +
                    "in: { $setUnion: [ \"$$value\", " +
                    "{ $map: { input: { $ifNull: [ \"$$this.weightedProducts\", [] ] }, as: \"bp\", in: \"$$bp._id\" } } " +
                    "] } " +
                    "} " +
                    "} " +
                    "} }",

            // Unisce in un unico array tutti gli ID
            "{ $project: { allProductIds: { $setUnion: [ \"$productsIds\", \"$bundlesProductIds\" ] } } }",

            // Lookup in products per ottenere i dettagli dei prodotti
            "{ $lookup: { from: \"products\", localField: \"allProductIds\", foreignField: \"_id\", as: \"products\" } }",
            "{ $unwind: \"$products\" }",

            // Esplodi l'array degli ingredientIds per ogni prodotto
            "{ $unwind: \"$products.ingredientIds\" }",

            // Lookup in ingredients per ottenere i dettagli dell'ingrediente (conversione con $toObjectId se necessario)
            "{ $lookup: { " +
                    "from: \"ingredients\", " +
                    "let: { ingId: { $toObjectId: \"$products.ingredientIds\" } }, " +
                    "pipeline: [ { $match: { $expr: { $eq: [ \"$_id\", \"$$ingId\" ] } } } ], " +
                    "as: \"ingredientDetails\" " +
                    "} }",
            "{ $unwind: \"$ingredientDetails\" }",

            // Filtra per ingredienti della categoria richiesta
            "{ $match: { \"ingredientDetails.categoryId\": ?2 } }",

            // Raggruppa per ottenere gli ingredienti distinti (utilizziamo l'ID presente in products.ingredientIds)
            "{ $group: { _id: \"$products.ingredientIds\" } }",

            // Conta il numero di ingredienti distinti
            "{ $group: { _id: null, distinctIngredientCount: { $sum: 1 } } }",

            // Proietta il risultato finale
            "{ $project: { _id: 0, distinctIngredientCount: 1 } }"
    })
    Optional<Long> countDistinctIngredientsByCategory(LocalDate pickupDate, OrderStatus status, String categoryId);


    @Aggregation(pipeline = {
            // Filtra per pickupDate e status
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // Espande la lista productsInPurchase
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei prodotti
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",

            // Calcola gli ingredienti per i productsInPurchase, con controllo su ingredientIds
            "{ $addFields: { " +
                    "productIngredientCount: { $cond: { " +
                    "if: { $and: [ { $ne: ['$productDetails.ingredientIds', null] }, { $isArray: '$productDetails.ingredientIds' } ] }, " +
                    "then: { $multiply: [ { $size: '$productDetails.ingredientIds' }, '$productsInPurchase.quantity' ] }, " +
                    "else: 0 " +
                    "} } " +
                    "} }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Espande i weightedProducts all'interno di bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei weightedProducts dai products
            "{ $lookup: { from: 'products', localField: 'bundlesInPurchase.weightedProducts._id', foreignField: '_id', as: 'weightedProductDetails' } }",
            "{ $unwind: { path: '$weightedProductDetails', preserveNullAndEmptyArrays: true } }",

            // Calcola gli ingredienti per i weightedProducts, con controllo su ingredientIds
            "{ $addFields: { " +
                    "bundleIngredientCount: { $cond: { " +
                    "if: { $and: [ { $ne: ['$weightedProductDetails.ingredientIds', null] }, { $isArray: '$weightedProductDetails.ingredientIds' } ] }, " +
                    "then: { $multiply: [ { $size: '$weightedProductDetails.ingredientIds' }, '$bundlesInPurchase.weightedProducts.quantity' ] }, " +
                    "else: 0 " +
                    "} } " +
                    "} }",

            // Combina i risultati dei productsInPurchase e bundlesInPurchase
            "{ $group: { _id: null, totalIngredientsUsed: { $sum: { $add: ['$productIngredientCount', '$bundleIngredientCount'] } } } }",

            // Proietta il risultato finale
            "{ $project: { _id: 0, totalIngredientsUsed: 1 } }"
    })
    Optional<Long> countTotalIngredientsByCategory(LocalDate date, OrderStatus status, String categoryId);


    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // Unwind prodotti acquistati e unione con products
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$productDetails.ingredientIds', preserveNullAndEmptyArrays: true } }",

            // Unwind bundle acquistati e unione con products
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",
            "{ $lookup: { from: 'products', localField: 'bundlesInPurchase.weightedProducts._id', foreignField: '_id', as: 'bundleProductDetails' } }",
            "{ $unwind: { path: '$bundleProductDetails', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundleProductDetails.ingredientIds', preserveNullAndEmptyArrays: true } }",

            // Filtro per l'ingrediente specifico
            "{ $match: { $or: [ { 'productDetails.ingredientIds': ?2 }, { 'bundleProductDetails.ingredientIds': ?2 } ] } }",

            // Calcolo dell'uso dell'ingrediente nei prodotti e nei bundle
            "{ $project: { ingredientUsage: { $sum: [ { $multiply: ['$productsInPurchase.quantity', 1] }, { $multiply: ['$bundlesInPurchase.quantity', 1] } ] } } }",

            // Somma totale dell'uso dell'ingrediente
            "{ $group: { _id: null, totalUsage: { $sum: '$ingredientUsage' } } }",
            "{ $project: { _id: 0, totalUsage: 1 } }"
    })
    Optional<Long> countIngredientUsage(LocalDate date, OrderStatus status, String ingredientId);


    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",
            "{ $project: { bundleCount: { $size: { $ifNull: [\"$bundlesInPurchase\", []] } } } }",
            "{ $group: { _id: null, totalBundles: { $sum: \"$bundleCount\" } } }",
            "{ $project: { _id: 0, totalBundles: { $ifNull: [\"$totalBundles\", 0] } } }"
    })
    Optional<Long> countTraysByDate(LocalDate date, OrderStatus status);

    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",
            "{ $unwind: { path: \"$bundlesInPurchase\", preserveNullAndEmptyArrays: true } }",
            "{ $group: { _id: null, totalPrice: { $sum: { $ifNull: [\"$bundlesInPurchase.totalPrice\", 0] } } } }",
            "{ $project: { _id: 0, totalPrice: 1 } }"
    })
    Optional<Double> countTotalPriceTraysByDate(LocalDate date, OrderStatus status);


    @Aggregation(pipeline = {
            // 1. Filtra gli ordini per data e stato
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // 2. Scompone l'array bundlesInPurchase
            "{ $unwind: { path: \"$bundlesInPurchase\", preserveNullAndEmptyArrays: true } }",

            // 3. Somma il peso moltiplicato per la quantità di ogni bundle
            "{ $group: { _id: null, totalWeightSum: { $sum: { $multiply: [ { $toDouble: { $ifNull: [\"$bundlesInPurchase.measure.weight\", 0.0] } }, { $ifNull: [\"$bundlesInPurchase.quantity\", 1] } ] } } } }",

            // 4. Proietta il risultato garantendo che sia un valore numerico
            "{ $project: { _id: 0, totalWeightSum: { $ifNull: [\"$totalWeightSum\", 0.0] } } }"
    })
    Optional<Double> countTrayWeightByDate(LocalDate date, OrderStatus status);

    @Aggregation(pipeline = {
            // 1. Filtra gli ordini per data e stato dell'ordine
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // 2. Scompone l'array bundlesInPurchase
            "{ $unwind: { path: \"$bundlesInPurchase\", preserveNullAndEmptyArrays: true } }",

            // 3. Somma il totalPrice moltiplicato per la quantità del bundle
            "{ $group: { _id: null, totalRevenue: { $sum: { $multiply: [ { $toDouble: { $ifNull: [\"$bundlesInPurchase.totalPrice\", 0.0] } }, { $ifNull: [\"$bundlesInPurchase.quantity\", 1] } ] } } } }",

            // 4. Proietta il risultato garantendo che sia un valore numerico
            "{ $project: { _id: 0, totalRevenue: { $ifNull: [\"$totalRevenue\", 0.0] } } }"
    })
    Optional<Double> countTotalRevenueByDate(LocalDate date, OrderStatus status);



    @Aggregation(pipeline = {
            // 1. Filtra gli ordini per data e stato dell'ordine
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // 2. Scompone l'array bundlesInPurchase
            "{ $unwind: { path: \"$bundlesInPurchase\", preserveNullAndEmptyArrays: true } }",

            // 3. Scompone l'array weightedProducts
            "{ $unwind: { path: \"$bundlesInPurchase.weightedProducts\", preserveNullAndEmptyArrays: true } }",

            // 4. Somma la quantità dei weightedProducts moltiplicata per la quantità del bundle
            "{ $group: { _id: null, totalQuantity: { $sum: { $multiply: [ { $ifNull: [\"$bundlesInPurchase.weightedProducts.quantity\", 0] }, { $ifNull: [\"$bundlesInPurchase.quantity\", 1] } ] } } } }",

            // 5. Proietta il risultato garantendo che sia un valore numerico
            "{ $project: { _id: 0, totalQuantity: { $ifNull: [\"$totalQuantity\", 0] } } }"
    })
    Optional<Long> countTotalWeightedProductsByDate(LocalDate date, OrderStatus status);


    @Aggregation(pipeline = {
            // 1. Filtro per data e stato
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // 2. bundle deve esistere ed essere non vuoto
            "{ $match: { 'bundlesInPurchase': { $exists: true, $ne: [] } } }",

            // 3. Srotola bundles
            "{ $unwind: '$bundlesInPurchase' }",

            // 4. Solo quelli con _id valido
            "{ $match: { 'bundlesInPurchase._id': { $exists: true, $ne: null } } }",

            // 5. Converte l'ObjectId in stringa
            "{ $project: { trayId: { $toString: '$bundlesInPurchase._id' } } }",

            // 6. Raggruppa per trayId per eliminare duplicati
            "{ $group: { _id: '$trayId' } }",

            // 7. Proietta lista semplice
            "{ $project: { _id: 0, trayId: '$_id' } }"
    })
    List<String> findDistinctTrayIdsByDate(LocalDate date, OrderStatus orderStatus);



    @Aggregation(pipeline = {
            // 1. Filtra gli ordini per data e stato dell'ordine
            "{ $match: { pickupDate: ?1, status: ?2 } }",

            // 2. Scompone l'array bundlesInPurchase per elaborare ogni bundle singolarmente
            "{ $unwind: \"$bundlesInPurchase\" }",

            // 3. Filtra solo il bundle con l'ID specificato
            "{ $match: { \"bundlesInPurchase._id\": ?0 } }",

            // 4. Somma tutte le quantità di quel tray
            "{ $group: { _id: null, totalQuantity: { $sum: { $ifNull: [\"$bundlesInPurchase.quantity\", 0] } } } }",

            // 5. Proietta solo il valore totale della quantità
            "{ $project: { _id: 0, totalQuantity: 1 } }"
    })
    Optional<Long> countQuantityByTrayIdAndDate(String id, LocalDate date, OrderStatus orderStatus);

    @Aggregation(pipeline = {
            // 1. Filtra gli ordini per data e stato dell'ordine
            "{ $match: { pickupDate: ?1, status: ?2 } }",

            // 2. Scompone l'array bundlesInPurchase
            "{ $unwind: { path: \"$bundlesInPurchase\", preserveNullAndEmptyArrays: true } }",

            // 3. Filtra solo il bundle con l'ID specificato
            "{ $match: { \"bundlesInPurchase._id\": ?0 } }",

            // 4. Somma il totalPrice moltiplicato per la quantità del bundle
            "{ $group: { _id: null, totalRevenue: { $sum: { $multiply: [ { $toDouble: { $ifNull: [\"$bundlesInPurchase.totalPrice\", 0.0] } }, { $ifNull: [\"$bundlesInPurchase.quantity\", 1] } ] } } } }",

            // 5. Proietta il risultato garantendo che sia un valore numerico
            "{ $project: { _id: 0, totalRevenue: { $ifNull: [\"$totalRevenue\", 0.0] } } }"
    })
    Optional<Double> countTotalRevenueByTrayIdAndDate(String trayId, LocalDate date, OrderStatus orderStatus);

    @Aggregation(pipeline = {
            // 1. Filtra gli ordini per data e stato dell'ordine
            "{ $match: { pickupDate: ?1, status: ?2 } }",

            // 2. Scompone l'array bundlesInPurchase per elaborare ogni tray separatamente
            "{ $unwind: { path: \"$bundlesInPurchase\", preserveNullAndEmptyArrays: true } }",

            // 3. Filtra solo il bundle con l'ID specificato
            "{ $match: { \"bundlesInPurchase._id\": ?0 } }",

            // 4. Somma il weight moltiplicato per la quantità del tray
            "{ $group: { _id: null, totalWeight: { $sum: { $multiply: [ { $toDouble: { $ifNull: [\"$bundlesInPurchase.measure.weight\", 0.0] } }, { $ifNull: [\"$bundlesInPurchase.quantity\", 1] } ] } } } }",

            // 5. Proietta il risultato garantendo che sia un valore numerico
            "{ $project: { _id: 0, totalWeight: { $ifNull: [\"$totalWeight\", 0.0] } } }"
    })
    Optional<Double> countTotalWeightByTrayIdAndDate(String trayId, LocalDate date, OrderStatus orderStatus);

    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",
            "{ $project: { productId: '$bundlesInPurchase.weightedProducts._id' } }",
            "{ $group: { _id: '$productId' } }",
            "{ $project: { _id: 0, productId: '$_id' } }"
    })
    List<String> findDistinctTrayProductWeightedIdByDate(LocalDate pickupDate, OrderStatus orderStatus);

    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?1, status: ?2 } }",
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",
            "{ $match: { $expr: { $eq: [ { $toString: '$bundlesInPurchase.weightedProducts._id' }, ?0 ] } } }",
            "{ $project: { quantity: { $ifNull: ['$bundlesInPurchase.weightedProducts.quantity', 0] } } }",
            "{ $group: { _id: null, totalQuantity: { $sum: '$quantity' } } }",
            "{ $project: { _id: 0, totalQuantity: 1 } }"
    })
    Optional<Long> countTrayProductWeightedById(String productWeightedId, LocalDate date, OrderStatus orderStatus);

    @Aggregation(pipeline = {
            // 1. Filtro per pickupDate e status
            "{ $match: { pickupDate: ?1, status: ?2 } }",

            // 2. Srotola bundles e weightedProducts
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",

            // 3. Filtro per productWeightedId (con $toString)
            "{ $match: { $expr: { $eq: [ { $toString: '$bundlesInPurchase.weightedProducts._id' }, ?0 ] } } }",

            // 4. Project quantity (cast a double) + productId
            "{ $project: { " +
                    "productId: '$bundlesInPurchase.weightedProducts._id', " +
                    "quantity: { $toDouble: { $ifNull: ['$bundlesInPurchase.weightedProducts.quantity', 0] } } " +
                    "} }",

            // 5. Somma quantity per prodotto
            "{ $group: { _id: '$productId', totalQuantity: { $sum: '$quantity' } } }",

            // 6. Join con 'products'
            "{ $lookup: { from: 'products', localField: '_id', foreignField: '_id', as: 'productData' } }",
            "{ $unwind: { path: '$productData', preserveNullAndEmptyArrays: true } }",

            // 7. Calcola totalWeight = totalQuantity * product.weight
            "{ $project: { totalWeight: { $multiply: ['$totalQuantity', { $toDouble: { $ifNull: ['$productData.weight', 0] } }] } } }"
    })
    Optional<Double> countWeightTrayProductWeightedById(String productWeightedId, LocalDate date, OrderStatus orderStatus);


    @Aggregation(pipeline = {
            // 1. Filtro per data e stato
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // 2. Srotola i bundles
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // 3. Proietta la label
            "{ $project: { label: '$bundlesInPurchase.measure.label' } }",

            // 4. Rimuovi duplicati raggruppando per label
            "{ $group: { _id: '$label' } }",

            // 5. Proietta come campo semplice per Spring
            "{ $project: { _id: 0, label: '$_id' } }"
    })
    List<String> findDistinctTrayMeasureByDate(LocalDate date, OrderStatus orderStatus);

    @Aggregation(pipeline = {
            // 1. Filtro per data e stato
            "{ $match: { pickupDate: ?1, status: ?2 } }",

            // 2. Srotola i bundle
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // 3. Estrai label e quantity in un project intermedio
            "{ $project: { " +
                    "label: '$bundlesInPurchase.measure.label', " +
                    "quantity: { $ifNull: ['$bundlesInPurchase.quantity', 0] }" +
                    "} }",

            // 4. Filtra solo le label richieste
            "{ $match: { label: ?0 } }",

            // 5. Somma le quantity
            "{ $group: { _id: null, totalQuantity: { $sum: '$quantity' } } }",

            // 6. Output pulito
            "{ $project: { _id: 0, totalQuantity: 1 } }"
    })
    Optional<Long> countQuantityByTrayMeasureAndDate(String measure, LocalDate date, OrderStatus orderStatus);

}
