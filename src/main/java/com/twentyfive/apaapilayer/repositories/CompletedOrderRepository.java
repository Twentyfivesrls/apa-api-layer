package com.twentyfive.apaapilayer.repositories;

import com.paypal.orders.Order;
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
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",
            "{ $group: { _id: '$customerId' } }",
            "{ $count: 'uniqueCustomers' }"
    })
    Optional<Long> countDistinctCustomerIdByPickupDateAndStatus(LocalDate pickupDate, OrderStatus status);

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
            "{ $match: { status: ?1, pickupDate: ?2 } }",
            "{ $project: { " +
                    "    productIds: { $map: { input: '$productsInPurchase', as: 'product', in: '$$product._id' } }, " +
                    "    bundleIds: { $map: { input: '$bundlesInPurchase', as: 'bundle', in: '$$bundle._id' } } " +
                    "} }",
            "{ $lookup: { from: 'products', localField: 'productIds', foreignField: '_id', as: 'products' } }",
            "{ $lookup: { from: 'trays', localField: 'bundleIds', foreignField: '_id', as: 'trays' } }",
            "{ $project: { " +
                    "    filteredProductIds: { $filter: { input: '$products', as: 'product', cond: { $eq: ['$$product.categoryId', ?0] } } }, " +
                    "    filteredTrayIds: { $filter: { input: '$trays', as: 'tray', cond: { $eq: ['$$tray.categoryId', ?0] } } } " +
                    "} }",
            "{ $project: { " +
                    "    finalIds: { $concatArrays: [ " +
                    "        { $map: { input: '$filteredProductIds', as: 'product', in: '$$product._id' } }, " +
                    "        { $map: { input: '$filteredTrayIds', as: 'tray', in: '$$tray._id' } } " +
                    "    ] } " +
                    "} }",
            "{ $unwind: '$finalIds' }",
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

            "{'$match': {'pickupDate': ?0, 'status': ?1}}",

            "{'$lookup': {'from': 'products', 'localField': 'productsInPurchase._id', 'foreignField': '_id', 'as': 'productDetails'}}",

            "{'$lookup': {'from': 'trays', 'localField': 'bundlesInPurchase._id', 'foreignField': '_id', 'as': 'bundleDetails'}}",

            "{'$project': {'productCategoryIds': {'$map': {'input': '$productDetails', 'as': 'product', 'in': '$$product.categoryId'}}, 'bundleCategoryIds': {'$map': {'input': '$bundleDetails', 'as': 'bundle', 'in': '$$bundle.categoryId'}}}}",

            "{'$addFields': {'allCategoryIds': {'$setUnion': ['$productCategoryIds', '$bundleCategoryIds']}}}",

            "{'$unwind': '$allCategoryIds'}",

            "{'$group': {'_id': '$allCategoryIds'}}",

            "{'$project': {'_id': 0, 'categoryId': '$_id'}}"

    })
    List<String> findDistinctCategoryIdsByDateAndStatus(
            LocalDate date,
            OrderStatus status
    );


    @Aggregation(pipeline = {
            // Filtra per pickupDate e OrderStatus
            "{ $match: { 'pickupDate': ?0, 'status': ?1 } }",

            // Espande la lista productsInPurchase
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Proietta solo gli ingredientIds da entrambe le liste, gestendo i null con $ifNull
            "{ $project: { " +
                    "ingredientIds: { " +
                    "$concatArrays: [ " +
                    "{ $ifNull: ['$productsInPurchase.ingredientIds', []] }, " +
                    "{ $ifNull: ['$bundlesInPurchase.ingredientIds', []] } " +
                    "] " +
                    "} " +
                    "} }",

            // Aggiunge gli ingredientId unici
            "{ $project: { uniqueIngredientIds: { $setUnion: ['$ingredientIds', []] } } }",

            // Raggruppa tutto in un unico documento e conta gli ingredienti unici
            "{ $group: { _id: null, count: { $sum: { $size: '$uniqueIngredientIds' } } } }",

            // Proietta il risultato finale
            "{ $project: { _id: 0, count: 1 } }"
    })
    Optional<Long> countUniqueIngredients(
            LocalDate date,
            OrderStatus status
    );


    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",
            "{ $unwind: '$productsInPurchase' }",
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: '$productDetails' }",
            "{ $project: { ingredientCount: { $multiply: [ { $size: '$productDetails.ingredientIds' }, '$productsInPurchase.quantity' ] } } }",
            "{ $group: { _id: null, totalIngredients: { $sum: '$ingredientCount' } } }",
            "{ $project: { _id: 0, totalIngredients: 1 } }"
    })
    Optional<Long> calculateTotalIngredients(LocalDate date, OrderStatus status);

    @Aggregation(pipeline = {
            "{ $match: { pickupDate: ?0, status: ?1 } }",
            "{ $unwind: '$productsInPurchase' }",
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: '$productDetails' }",
            "{ $unwind: '$productDetails.ingredientIds' }",
            "{ $group: { _id: '$productDetails.ingredientIds' } }",
            "{ $project: { _id: 0, ingredientId: '$_id' } }"
    })
    List<String> findDistinctIngredientIds(LocalDate date, OrderStatus status);

    @Aggregation(pipeline = {
            // Filtra per pickupDate e status
            "{ $match: { pickupDate: ?0, status: ?1 } }",

            // Espande la lista productsInPurchase
            "{ $unwind: { path: '$productsInPurchase', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei prodotti
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: { path: '$productDetails', preserveNullAndEmptyArrays: true } }",

            // Espande gli ingredienti dei prodotti
            "{ $unwind: { path: '$productDetails.ingredientIds', preserveNullAndEmptyArrays: true } }",

            // Espande la lista bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase', preserveNullAndEmptyArrays: true } }",

            // Espande i weightedProducts all'interno di bundlesInPurchase
            "{ $unwind: { path: '$bundlesInPurchase.weightedProducts', preserveNullAndEmptyArrays: true } }",

            // Lookup per ottenere i dettagli dei weightedProducts
            "{ $lookup: { from: 'products', localField: 'bundlesInPurchase.weightedProducts._id', foreignField: '_id', as: 'weightedProductDetails' } }",
            "{ $unwind: { path: '$weightedProductDetails', preserveNullAndEmptyArrays: true } }",

            // Espande gli ingredienti dei weightedProducts
            "{ $unwind: { path: '$weightedProductDetails.ingredientIds', preserveNullAndEmptyArrays: true } }",

            // Unione degli ingredienti da productsInPurchase e weightedProducts
            "{ $project: { ingredientId: { $ifNull: ['$productDetails.ingredientIds', '$weightedProductDetails.ingredientIds'] } } }",

            // Raggruppa per ingredientId distinti
            "{ $group: { _id: '$ingredientId' } }",

            // Conta gli ingredienti distinti
            "{ $group: { _id: null, distinctIngredientsCount: { $sum: 1 } } }",

            // Proietta il risultato finale
            "{ $project: { _id: 0, distinctIngredientsCount: 1 } }"
    })
    Optional<Long> countDistinctIngredients(LocalDate date, OrderStatus status);


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
            "{ $unwind: '$productsInPurchase' }",
            "{ $lookup: { from: 'products', localField: 'productsInPurchase._id', foreignField: '_id', as: 'productDetails' } }",
            "{ $unwind: '$productDetails' }",
            "{ $unwind: '$productDetails.ingredientIds' }",
            "{ $match: { 'productDetails.ingredientIds': ?2 } }",
            "{ $project: { ingredientUsage: { $multiply: [1, '$productsInPurchase.quantity'] } } }",
            "{ $group: { _id: null, totalUsage: { $sum: '$ingredientUsage' } } }",
            "{ $project: { _id: 0, totalUsage: 1 } }"
    })
    Optional<Long> countIngredientUsage(LocalDate date, OrderStatus status, String ingredientId);

}
